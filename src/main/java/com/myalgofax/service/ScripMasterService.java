package com.myalgofax.service;


import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.myalgofax.brockerAPI.config.KotakApiConfig;
import com.myalgofax.dto.ScripMasterDTO;
import com.myalgofax.exceptions.BrokerApiException;
import com.myalgofax.repository.ScripMasterRepository;
import com.myalgofax.security.util.jwt.JwtDecoder;
import com.myalgofax.security.util.jwt.JwtUtil;
import com.myalgofax.user.entity.ScripMaster;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ScripMasterService {
	
	private static final Logger logger = LoggerFactory.getLogger(ScripMasterService.class);

    @Autowired
    private ScripMasterRepository scripMasterRepository;
    
    @Autowired
	private JwtUtil jwtUtil;
    
    @Autowired
    private KotakApiConfig kotakApiConfig;
    
    @Autowired
	private WebClient webClient;
 
    
    
    public Mono<Map<String, Object>> getAllScrips(ScripMasterDTO request) {
        logger.info("enter in ScripMasterService");

        return scripMasterRepository.count()
            .flatMap(count -> {
                if (count > 0) {
                    logger.info("Scrip data already exists. Skipping fetch.");
                    return Mono.just(Map.of("message", "Scrip data already exists"));
                }

                String accessBrokerToken = request.getAccessBrokerToken();
                Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

                Object step1Obj = brokerAccessToken.get("kotakTokenStep1");
                Object step2Obj = brokerAccessToken.get("kotakTokenStep2");
                
                if (step1Obj == null || step2Obj == null) {
                    return Mono.error(new IllegalStateException("Invalid broker access token"));
                }
                
                String step1 = step1Obj.toString();
                String step2 = step2Obj.toString();

                try {
                    JwtDecoder.getSubClaim(step2);
                } catch (Exception e) {
                    logger.error("Failed to extract sub from JWT", e);
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JWT token"));
                }
                String filePath = kotakApiConfig.getFilePath();
                if (filePath == null || filePath.isBlank()) {
                    return Mono.error(new IllegalStateException("kotak.api.file-path is not configured"));
                }
                return webClient.get()
                    .uri(filePath)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + step1)
                    .header(HttpHeaders.ACCEPT, "*/*")                    
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMapMany((String response) -> {
                        try {
                            JsonNode data = new ObjectMapper().readTree(response).path("data");
                            ArrayNode filesPaths = (ArrayNode) data.path("filesPaths");

                            List<String> fileUrls = new ArrayList<>();
                            filesPaths.forEach(path -> fileUrls.add(path.asText()));

                            return Flux.fromIterable(fileUrls);
                        } catch (Exception e) {
                            logger.error("Error parsing file-paths response", e);
                            return Flux.error(new BrokerApiException("Failed to parse file-paths", e));
                        }
                    })

                    .flatMap(this::downloadAndSaveCsv)
                    .then(Mono.just(Map.of("message", "Scrip data fetched and saved successfully")));
            });
    }


    
    private Mono<Void> downloadAndSaveCsv(String fileUrl) {
    	logger.info("Processing file URL");
        return webClient.get()
            .uri(fileUrl)
            .retrieve()
            .bodyToMono(String.class)
            .flatMap(csvContent -> Mono.fromRunnable(() -> {
                List<ScripMaster> entities = parseCsvToEntities(csvContent);
                scripMasterRepository.saveAll(entities); // blocking call
            }))
            .then()
            .onErrorResume(e -> {
                logger.error("Failed to download or save CSV", e);
                return Mono.empty();
            });
    }


    private List<ScripMaster> parseCsvToEntities(String csvContent) {
        List<ScripMaster> entities = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(new StringReader(csvContent))
                .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
                .build()) {

            String[] rawHeaders = reader.readNext();
            if (rawHeaders == null) return entities;

            String[] headers = Arrays.stream(rawHeaders)
                .map(h -> h.trim().replaceAll(";$", ""))
                .toArray(String[]::new);

            // Validate headers exist in FIELD_MAPPERS
            for (String header : headers) {
                if (!FIELD_MAPPERS.containsKey(header)) {
                    logger.warn("Unexpected header found in CSV");
                }
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                ScripMaster entity = new ScripMaster();

                for (int i = 0; i < headers.length && i < row.length; i++) {
                    String header = headers[i];
                    String value = row[i].trim();

                    BiConsumer<ScripMaster, String> setter = FIELD_MAPPERS.get(header);
                    if (setter != null) {
                        try {
                            setter.accept(entity, value);
                        } catch (Exception e) {
                            logger.warn("Invalid value found in CSV data", e);
                        }
                    }
                }

                // Skip incomplete entries
                if (entity.getPSymbol() != null) {
                    entities.add(entity);
                }
            }

        } catch (Exception e) {
            logger.error("CSV parsing failed", e);
            throw new RuntimeException("Failed to parse CSV data", e);
        }

        return entities;
    }



    private Date convertEpochToDate(Long epoch, String exchange) {
        if (epoch == null || epoch <= 0) return null;

        // Offset for Kotak-specific epoch adjustment (only for certain exchanges)
        // This offset converts from Kotak's custom epoch base to Unix epoch (Jan 1, 1970)
        final long KOTAK_EPOCH_OFFSET = 315513000L; // ~10 years in seconds
        
        if ("nse_fo".equalsIgnoreCase(exchange) || "cde_fo".equalsIgnoreCase(exchange)) {
            epoch += KOTAK_EPOCH_OFFSET;
        }

        return new Date(epoch * 1000); // Convert seconds to milliseconds
    }
    
    
    private static BigDecimal parseStrikePrice(String value) {
        try {
            return value == null || value.isBlank()
                ? null
                : new BigDecimal(value).divide(BigDecimal.valueOf(100));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    
    private static BigDecimal parseBigDecimal(String value) {
        try {
            return value == null || value.isBlank() ? null : new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    
    private static final Map<String, BiConsumer<ScripMaster, String>> FIELD_MAPPERS = Map.ofEntries(
    	    Map.entry("pSymbolName", (e, v) -> e.setpSymbolName(v)),
    	    Map.entry("pTrdSymbol", (e, v) -> e.setPTrdSymbol(v)),
    	    Map.entry("pOptionType", (e, v) -> e.setPOptionType(v)),
    	    Map.entry("pDesc", (e, v) -> e.setPDesc(v)),
    	    
    	    Map.entry("dStrikePrice", (e, v) -> e.setDStrikePrice(parseStrikePrice(v))),

    	    Map.entry("pSegment", (e, v) -> e.setPSegment(v)),
    	    Map.entry("dHighPriceRange", (e, v) -> e.setDHighPriceRange(parseBigDecimal(v))),
    	    Map.entry("dOpenInterest", (e, v) -> e.setDOpenInterest(parseBigDecimal(v))),
    	    Map.entry("dLowPriceRange", (e, v) -> e.setDLowPriceRange(parseBigDecimal(v))),
    	    Map.entry("pSymbol", (e, v) -> e.setPSymbol(parseBigDecimal(v))),
    	    Map.entry("pExchange", (e, v) -> e.setPExchange(v))
    	);


}
