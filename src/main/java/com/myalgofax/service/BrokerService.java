package com.myalgofax.service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myalgofax.brockerAPI.config.KotakApiConfig;
import com.myalgofax.dto.BrokerDto;
import com.myalgofax.dto.CancelBracketOrderDto;
import com.myalgofax.dto.CancelCoverOrderDto;
import com.myalgofax.dto.CancelOrderDto;
import com.myalgofax.dto.LimitsDto;
import com.myalgofax.dto.MarginDto;
import com.myalgofax.dto.ModifyOrderDto;
import com.myalgofax.dto.PlaceOrderDto;
import com.myalgofax.dto.PositionsDto;
import com.myalgofax.exceptions.BrokerApiException;
import com.myalgofax.repository.BrokerRepository;
import com.myalgofax.repository.UserRepository;
import com.myalgofax.security.util.jwt.JwtDecoder;
import com.myalgofax.security.util.jwt.JwtUtil;
import com.myalgofax.user.entity.Broker;

import reactor.core.publisher.Mono;

@Service
public class BrokerService {

	private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private KotakApiConfig apiConfig;

	@Autowired
	private BrokerRepository brokerRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	@Qualifier("oauthWebClient")
	private WebClient webClient;
	
	
	@Value("${kotak.api.oauth-url}")
	private String oauthUrl;

	@Value("${kotak.api.login-url}")
	private String loginUrl;

	@Value("${kotak.api.generate-otp-url}")
	private String generateOtpUrl;

	@Value("${kotak.api.otp-auth-url}")
	private String otpAuthUrl;

	@Value("${kotak.api.positions-url}")
	private String positionsUrl;

	@Value("${kotak.api.holdings-url}")
	private String holdingsUrl;

	@Value("${kotak.api.orders-url}")
	private String ordersUrl;

	@Value("${kotak.api.order-history-url}")
	private String orderHistoryUrl;

	@Value("${kotak.api.trades-url}")
	private String tradesUrl;

	@Value("${kotak.api.place-order-url}")
	private String placeOrderUrl;

	@Value("${kotak.api.modify-order-url}")
	private String modifyOrderUrl;

	@Value("${kotak.api.cancel-order-url}")
	private String cancelOrderUrl;

	@Value("${kotak.api.exit-cover-order-url}")
	private String exitCoverOrderUrl;

	@Value("${kotak.api.exit-bracket-order-url}")
	private String exitBracketOrderUrl;

	@Value("${kotak.api.limits-url}")
	private String limitsUrl;

	@Value("${kotak.api.check-margin-url}")
	private String checkMarginUrl;

	public Mono<Map<String, Object>> brokerlogin(BrokerDto request) {
		logger.info("Received broker login request for brokerCode: {}", request.getBrokerCode());

		// Get API credentials from request
		String apiKey = request.getApiKey();
		String apiSecret = request.getApiSecret();
		String password = request.getPassword();

		return ReactiveSecurityContextHolder.getContext().map(ctx -> (String) ctx.getAuthentication().getCredentials())
				.flatMap(userId -> {
					logger.debug("Authenticated userId: {}", userId);

					return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode())
							.flatMap(broker -> {
								logger.info("Broker already exists for userId {} with brokerCode {}", userId,
										request.getBrokerCode());
								return proceedWithKotakCalls(apiKey, apiSecret, password, broker, userId);
							})
							.switchIfEmpty(Mono.defer(() -> {
								logger.info("No broker found. Creating new broker for userId {}", userId);

								return userRepository.findByUserId(userId)
										.switchIfEmpty(Mono.error(new RuntimeException(
												"User not found with userId: " + userId)))
										.flatMap(user -> {
											// Create new broker
											Broker broker = new Broker();
											logger.info("request", request);
											broker.setConsumerkey(apiKey);
											broker.setConsumerSecretKey(apiSecret);
											broker.setPassword(password);
											broker.setPhoneNumber(request.getPhoneNumber());
											broker.setUserId(userId);
											broker.setUsername(userId);
											broker.setUcc(request.getClientId());
											broker.setBrokerCode(request.getBrokerCode());
											broker.setNeoFinKey(request.getNeoFinKey());
											broker.setActiveInv("N");

											logger.info("Saving new broker for userId {}", userId);

											return brokerRepository.save(broker)
													.flatMap(savedBroker -> proceedWithKotakCalls(apiKey, apiSecret,
															password, savedBroker, userId));
										});
							}));
				});
	}

	private Mono<Map<String, Object>> proceedWithKotakCalls(String apiKey, String apiSecret, String password,
			Broker broker, String userId) {
		logger.info("Proceeding with Kotak API calls for broker: {}", broker.getUserId());

		return callKotakOAuthApi(apiKey, apiSecret).flatMap(accessToken -> {
			logger.debug("Received Kotak accessToken (masked): {}****", accessToken.substring(0, 4));

			return callKotakViewToken(accessToken, broker.getPhoneNumber(), password).flatMap(viewTokenData -> {
				String viewToken = viewTokenData.get("token").toString();
				String sid = (String) viewTokenData.get("sid");
				String greetingName = (String) viewTokenData.get("greetingName");
				String fullName = (String) viewTokenData.get("fullName");
				String ucc = (String) viewTokenData.get("ucc");

				logger.debug("Received Kotak viewToken (masked): {}****", viewToken.substring(0, 4));

				return callKotakGenerateOtp(accessToken, viewToken).map(otpResponse -> {
					logger.info("Received OTP response from Kotak");

					String jwtToken = jwtUtil.generateBrokerAccessToken(accessToken, viewToken, sid, broker.getUserId(),
							broker.getBrokerCode());

					Map<String, Object> brokerInfo = new HashMap<>();
					brokerInfo.put("phoneNumber", broker.getPhoneNumber());
					brokerInfo.put("ucc", ucc);
					brokerInfo.put("userId", broker.getUserId());

					Map<String, Object> response = new HashMap<>();
					response.put("sid", sid);
					response.put("jwtToken", jwtToken);
					response.put("broker", brokerInfo);
					response.put("greetingName", greetingName);
					response.put("fullName", fullName);
					response.put("otpResponse", otpResponse);

					return response;
				});
			});
		});
	}

	private Mono<String> callKotakOAuthApi(String consumerKey, String consumerSecret) {
		String credentials = consumerKey + ":" + consumerSecret;
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

		logger.info("Calling Kotak OAuth API with masked consumerKey: {}****", consumerKey.substring(0, 4));

		return webClient.post().uri(oauthUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.header(HttpHeaders.AUTHORIZATION, basicAuth)
				.body(BodyInserters.fromFormData("grant_type", "client_credentials")).retrieve()
				.bodyToMono(String.class).map(response -> {
					logger.debug("OAuth API raw response received");

					try {
						JsonNode json = objectMapper.readTree(response);
						return json.path("access_token").asText();
					} catch (Exception e) {
						logger.error("Error parsing OAuth token response", e);
						throw new BrokerApiException("Failed to parse token response", e);
					}
				}).onErrorMap(e -> {
					logger.error("Error calling Kotak OAuth API", e);
					return new BrokerApiException("Failed to obtain token from Kotak API", e);
				});
	}

	private Mono<Map<String, Object>> callKotakViewToken(String accessToken, String mobileNumber, String password) {
		logger.info("Calling Kotak ViewToken API for mobileNumber: {}", mobileNumber);
		logger.info("Calling Kotak ViewToken API for password: {}", password);
		logger.info("Calling Kotak ViewToken API for accessToken: {}", accessToken);

		Map<String, String> body = Map.of("mobileNumber", mobileNumber, "password", password);

		return webClient.post().uri(loginUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.header(HttpHeaders.ACCEPT, "*/*").contentType(MediaType.APPLICATION_JSON).bodyValue(body).retrieve()
				.bodyToMono(String.class).map(response -> {
					try {
						JsonNode data = objectMapper.readTree(response).path("data");

						Map<String, Object> result = new HashMap<>();
						result.put("token", data.path("token").asText());
						result.put("sid", data.path("sid").asText());
						result.put("ucc", data.path("ucc").asText());
						result.put("greetingName", data.path("greetingName").asText());
						result.put("fullName", data.path("fullName").asText());

						return result;
					} catch (Exception e) {
						logger.error("Error parsing view token response", e);
						throw new BrokerApiException("Failed to parse view token", e);
					}
				});
	}

	private Mono<String> callKotakGenerateOtp(String accessToken, String viewToken) {
		logger.info("Calling Kotak Generate OTP API");

		try {
			if (viewToken == null || viewToken.isBlank()) {
				return Mono.error(new IllegalArgumentException("Empty view token received from Kotak"));
			}

			String[] parts = viewToken.trim().split("\\.");
			if (parts.length < 2) {
				return Mono.error(new IllegalArgumentException("Invalid JWT format"));
			}

			String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
			JsonNode json = objectMapper.readTree(payload);
			String userId = json.path("sub").asText();

			logger.debug("Extracted userId from viewToken");

			Map<String, Object> body = Map.of("userId", userId, "sendEmail", true, "isWhitelisted", true);

			return webClient.post().uri(generateOtpUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.header(HttpHeaders.ACCEPT, "*/*").contentType(MediaType.APPLICATION_JSON).bodyValue(body)
					.retrieve().bodyToMono(String.class);

		} catch (Exception e) {
			logger.error("Failed to parse or decode view token", e);
			return Mono.error(new BrokerApiException("Failed to generate OTP from Kotak", e));
		}
	}

	public Mono<Map<String, Object>> verifyotp(BrokerDto request) {
		logger.info("Verifying OTP for broker");

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();

		String sub;
		try {
			sub = JwtDecoder.getSubClaim(step2);
		} catch (Exception e) {
			logger.error("Failed to extract sub from JWT", e);
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JWT token"));
		}

		Map<String, Object> body = Map.of("otp", request.getOtp().trim(), "userId", sub.trim());

		return webClient.post().uri(otpAuthUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + step1)
				.header("Auth", step2).header("sid", sid).header(HttpHeaders.ACCEPT, "*/*")
				.contentType(MediaType.APPLICATION_JSON).bodyValue(body).retrieve().bodyToMono(String.class)
				.flatMap(response -> {
					try {
						JsonNode data = new ObjectMapper().readTree(response).path("data");

						Map<String, Object> result = new HashMap<>();
						result.put("token", data.path("token").asText());
						result.put("sid", data.path("sid").asText());
						result.put("ucc", data.path("ucc").asText());
						result.put("greetingName", data.path("greetingName").asText());

						// ✅ Update broker status in DB
						return ReactiveSecurityContextHolder.getContext()
								.map(ctx -> (String) ctx.getAuthentication().getCredentials())
								.flatMap(userId -> updateBrokerStatus(userId, request.getBrokerCode(), result));
					} catch (Exception e) {
						logger.error("Error parsing OTP verification response", e);
						return Mono.error(new BrokerApiException("Failed to parse OTP verification response", e));
					}
				});
	}

	public Mono<Map<String, Object>> checkBrokerStatus(BrokerDto request) {

		logger.info("enter in checkBrokerStatus service call");

		String brokerCode = request.getBrokerCode();
		logger.info("Processing broker status check");

		return ReactiveSecurityContextHolder.getContext().map(ctx -> (String) ctx.getAuthentication().getCredentials())
				.flatMap(userId -> brokerRepository.findByUserIdAndBrokerCode(userId, brokerCode)
						.switchIfEmpty(Mono.error(new RuntimeException("Broker not found for user")))
						.flatMap(broker -> {
							// Check if all required data is present
							if (broker.getConsumerkey() != null && broker.getConsumerSecretKey() != null
									&& broker.getPassword() != null && broker.getPhoneNumber() != null) {

								// Prepare request for brokerlogin
								BrokerDto loginRequest = new BrokerDto();
								loginRequest.setBrokerCode(broker.getBrokerCode());
								loginRequest.setClientId(broker.getUcc());
								loginRequest.setPhoneNumber(broker.getPhoneNumber());

								loginRequest.setApiKey(broker.getConsumerkey());
								loginRequest.setApiSecret(broker.getConsumerSecretKey());
								loginRequest.setPassword(broker.getPassword());

								logger.info("Broker data present, calling brokerlogin");
								return brokerlogin(loginRequest);
							} else {
								// Return status without login
								Map<String, Object> result = new HashMap<>();
								result.put("brokerCode", broker.getBrokerCode());
								result.put("phoneNumber", broker.getPhoneNumber());
								result.put("ucc", broker.getUcc());
								result.put("status",
										"Y".equalsIgnoreCase(broker.getActiveInv()) ? "verified" : "pending");
								logger.info("Broker status check completed");
								return Mono.just(result);
							}
						}));
	}

	private Mono<Map<String, Object>> updateBrokerStatus(String userId, String brokerCode, Map<String, Object> result) {
		return brokerRepository.updateActiveInvByUserIdAndBrokerCode(userId, brokerCode, "Y")
				.doOnNext(updated -> logger.info("Updated {} broker record(s) to activeInv=Y", updated))
				.thenReturn(result);
	}

	public Mono<Map<String, Object>> getPositions(PositionsDto request) {
		logger.info("Fetching positions for broker");

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

		return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
				.switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
				.flatMap(broker -> {
					String neoFinKey = broker.getNeoFinKey();

					return webClient.get().uri(positionsUrl).header(HttpHeaders.ACCEPT, "application/json")
							.header("Sid", sid).header("Auth", step2).header("neo-fin-key", neoFinKey)
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + step1).retrieve().bodyToMono(String.class)
							.map(response -> {
								try {
									JsonNode responseJson = objectMapper.readTree(response);
									Map<String, Object> result = new HashMap<>();
									result.put("positions", responseJson);
									return result;
								} catch (Exception e) {
									logger.error("Error parsing positions response", e);
									throw new BrokerApiException("Failed to parse positions response", e);
								}
							});
				});
	}

	public Mono<Map<String, Object>> getPortfolioHoldings(BrokerDto request) {
		logger.info("Fetching portfolio holdings for broker");

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

		return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
				.switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
				.flatMap(broker -> {
					return webClient.get().uri(holdingsUrl).header(HttpHeaders.ACCEPT, "*/*").header("sid", sid)
							.header("Auth", step2).header(HttpHeaders.AUTHORIZATION, "Bearer " + step1).retrieve()
							.bodyToMono(String.class).map(response -> {
								try {
									JsonNode responseJson = objectMapper.readTree(response);
									Map<String, Object> result = new HashMap<>();
									result.put("holdings", responseJson);
									return result;
								} catch (Exception e) {
									logger.error("Error parsing portfolio holdings response", e);
									throw new BrokerApiException("Failed to parse portfolio holdings response", e);
								}
							});
				});
	}

	public Mono<Map<String, Object>> getOrderBook(BrokerDto request) {
		logger.info("Fetching order book for broker");

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

		return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
				.switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
				.flatMap(broker -> {
					String neoFinKey = broker.getNeoFinKey();

					return webClient.get().uri(ordersUrl).header(HttpHeaders.ACCEPT, "application/json")
							.header("Sid", sid).header("Auth", step2).header("neo-fin-key", neoFinKey)
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + step1).retrieve().bodyToMono(String.class)
							.map(response -> {
								try {
									JsonNode responseJson = objectMapper.readTree(response);
									Map<String, Object> result = new HashMap<>();
									result.put("orders", responseJson);
									return result;
								} catch (Exception e) {
									logger.error("Error parsing order book response", e);
									throw new BrokerApiException("Failed to parse order book response", e);
								}
							});
				});
	}

	public Mono<Map<String, Object>> getOrderHistory(BrokerDto request) {
		logger.info("Fetching order history for broker");

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

		return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
				.switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
				.flatMap(broker -> {
					String neoFinKey = broker.getNeoFinKey();

					String formData = "jData={\"nOrdNo\":\"" + request.getOtp() + "\"}";

					return webClient.post().uri(orderHistoryUrl).header(HttpHeaders.ACCEPT, "application/json")
							.header("Sid", sid).header("Auth", step2).header("neo-fin-key", neoFinKey)
							.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + step1).bodyValue(formData).retrieve()
							.bodyToMono(String.class).map(response -> {
								try {
									JsonNode responseJson = objectMapper.readTree(response);
									Map<String, Object> result = new HashMap<>();
									result.put("orderHistory", responseJson);
									return result;
								} catch (Exception e) {
									logger.error("Error parsing order history response", e);
									throw new BrokerApiException("Failed to parse order history response", e);
								}
							});
				});
	}

	public Mono<Map<String, Object>> getTradeBook(BrokerDto request) {
		logger.info("Fetching trade book for broker");

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

		return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
				.switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
				.flatMap(broker -> {
					String neoFinKey = broker.getNeoFinKey();

					return webClient.get().uri(tradesUrl).header(HttpHeaders.ACCEPT, "application/json")
							.header("Sid", sid).header("Auth", step2).header("neo-fin-key", neoFinKey)
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + step1).retrieve().bodyToMono(String.class)
							.map(response -> {
								try {
									JsonNode responseJson = objectMapper.readTree(response);
									Map<String, Object> result = new HashMap<>();
									result.put("trades", responseJson);
									return result;
								} catch (Exception e) {
									logger.error("Error parsing trade book response", e);
									throw new BrokerApiException("Failed to parse trade book response", e);
								}
							});
				});
	}

	public Mono<Map<String, Object>> placeOrder(PlaceOrderDto request) {
		logger.info("Placing order for broker");

		if (!"nse_fo".equals(request.getEs())) {
			return Mono.error(new IllegalArgumentException("Invalid segment for F&O order"));
		}

		if (!Arrays.asList("NRML", "MIS", "CO").contains(request.getPc())) {
			return Mono.error(new IllegalArgumentException("Invalid product code for F&O"));
		}

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

		return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
				.switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
				.flatMap(broker -> {
					String neoFinKey = broker.getNeoFinKey();

					String orderData = String.format(
							"{\"am\":\"%s\", \"dq\":\"%s\", \"es\":\"%s\", \"mp\":\"%s\", \"pc\":\"%s\", \"pf\":\"%s\", \"pr\":\"%s\", \"pt\":\"%s\", \"qt\":\"%s\", \"rt\":\"%s\", \"tp\":\"%s\", \"ts\":\"%s\", \"tt\":\"%s\", "
									+ "\"bc\":\"%s\", \"cf\":\"%s\", \"dd\":\"%s\", \"mq\":\"%s\", \"nc\":\"%s\", \"pa\":\"%s\", \"ot\":\"%s\", \"tk\":\"%s\", \"ur\":\"%s\", \"cl\":\"%s\", \"od\":\"%s\", "
									+ "\"slv\":\"%s\", \"sot\":\"%s\", \"slt\":\"%s\", \"sov\":\"%s\", \"lat\":\"%s\", \"tlt\":\"%s\", \"tsv\":\"%s\", \"ig\":\"%s\"}",
							request.getAm(), request.getDq(), request.getEs(), request.getMp(), request.getPc(),
							request.getPf(), Double.parseDouble(request.getPr()), request.getPt(), request.getQt(),
							request.getRt(), request.getTp(), request.getTs(), request.getTt(), request.getBc(),
							request.getCf(), request.getDd(), request.getMq(), request.getNc(),

							request.getEs().equals("nse_fo") ? "NRML:NRML||MIS:MIS||CO:CO"
									: "BO:BO||CNC:CNC||CO:CO||NRML:NRML",
							request.getOt(), request.getTk(), request.getUr(), request.getCl(), request.getOd(),
							request.getSlv(), request.getSot(), request.getSlt(), request.getSov(), request.getLat(),
							request.getTlt(), request.getTsv(), request.getIg());

					String formData = "jData=" + orderData;

					return webClient.post().uri(placeOrderUrl).header(HttpHeaders.ACCEPT, "application/json")
							.header("Sid", sid).header("Auth", step2).header("neo-fin-key", neoFinKey)
							.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + step1).bodyValue(formData).retrieve()
							.bodyToMono(String.class).map(response -> {
								try {
									JsonNode responseJson = objectMapper.readTree(response);
									Map<String, Object> result = new HashMap<>();
									result.put("orderResponse", responseJson);
									return result;
								} catch (Exception e) {
									logger.error("Error parsing place order response", e);
									throw new BrokerApiException("Failed to parse place order response", e);
								}
							});
				});
	}

	public Mono<Map<String, Object>> modifyOrder(ModifyOrderDto request) {
		logger.info("Modifying order for broker");

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

		return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
				.switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
				.flatMap(broker -> {
					String neoFinKey = broker.getNeoFinKey();

					String orderData = String.format(
							"{\"tk\":\"%s\", \"mp\":\"%s\", \"pc\":\"%s\", \"dd\":\"%s\", \"dq\":\"%s\", \"vd\":\"%s\", \"ts\":\"%s\", \"tt\":\"%s\", \"pr\":\"%s\", \"tp\":\"%s\", \"qt\":\"%s\", \"no\":\"%s\", \"es\":\"%s\", \"pt\":\"%s\"}",
							request.getTk(), request.getMp(), request.getPc(), request.getDd(), request.getDq(),
							request.getVd(), request.getTs(), request.getTt(), Double.parseDouble(request.getPr()),
							request.getTp(), request.getQt(), request.getNo(), request.getEs(), request.getPt());

					String formData = "jData=" + orderData;

					return webClient.post().uri(modifyOrderUrl).header(HttpHeaders.ACCEPT, "application/json")
							.header("Auth", step2).header("Sid", sid).header("neo-fin-key", neoFinKey)
							.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + step1).bodyValue(formData).retrieve()
							.bodyToMono(String.class).map(response -> {
								try {
									JsonNode responseJson = objectMapper.readTree(response);
									Map<String, Object> result = new HashMap<>();
									result.put("modifyResponse", responseJson);
									return result;
								} catch (Exception e) {
									logger.error("Error parsing modify order response", e);
									throw new BrokerApiException("Failed to parse modify order response", e);
								}
							});
				});
	}

	public Mono<Map<String, Object>> cancelOrder(CancelOrderDto request) {
		logger.info("Cancelling order for broker");

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

		return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
				.switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
				.flatMap(broker -> {
					String neoFinKey = broker.getNeoFinKey();

					StringBuilder orderDataBuilder = new StringBuilder("{\"on\":\"").append(request.getOn())
							.append("\"");
					if (request.getAm() != null && !request.getAm().isEmpty()) {
						orderDataBuilder.append(", \"am\":\"").append(request.getAm()).append("\"");
					}
					if (request.getTs() != null && !request.getTs().isEmpty()) {
						orderDataBuilder.append(", \"ts\":\"").append(request.getTs()).append("\"");
					}
					orderDataBuilder.append("}");

					String formData = "jData=" + orderDataBuilder.toString();
					logger.info(orderDataBuilder.toString());
					return webClient.post().uri(cancelOrderUrl).header(HttpHeaders.ACCEPT, "application/json")
							.header("Sid", sid).header("Auth", step2).header("neo-fin-key", neoFinKey)
							.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + step1).bodyValue(formData).retrieve()
							.bodyToMono(String.class).map(response -> {
								try {
									JsonNode responseJson = objectMapper.readTree(response);
									logger.info("responseJson: " + responseJson);
									Map<String, Object> result = new HashMap<>();
									result.put("cancelResponse", responseJson);
									return result;
								} catch (Exception e) {
									logger.error("Error parsing cancel order response", e);
									throw new BrokerApiException("Failed to parse cancel order response", e);
								}
							});
				});
	}

	public Mono<Map<String, Object>> cancelCoverOrder(CancelCoverOrderDto request) {
		logger.info("Cancelling cover order for broker");

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

		return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
				.switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
				.flatMap(broker -> {
					String neoFinKey = broker.getNeoFinKey();

					String orderData = String.format("{\"am\":\"%s\", \"on\":\"%s\"}", request.getAm(),
							request.getOn());
					String formData = "jData=" + orderData;

					return webClient.post().uri(exitCoverOrderUrl).header(HttpHeaders.ACCEPT, "application/json")
							.header("Sid", sid).header("Auth", step2).header("neo-fin-key", neoFinKey)
							.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + step1).bodyValue(formData).retrieve()
							.bodyToMono(String.class).map(response -> {
								try {
									JsonNode responseJson = objectMapper.readTree(response);
									Map<String, Object> result = new HashMap<>();
									result.put("cancelCoverResponse", responseJson);
									return result;
								} catch (Exception e) {
									logger.error("Error parsing cancel cover order response", e);
									throw new BrokerApiException("Failed to parse cancel cover order response", e);
								}
							});
				});
	}

	public Mono<Map<String, Object>> cancelBracketOrder(CancelBracketOrderDto request) {
		logger.info("Cancelling bracket order for broker");

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

		return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
				.switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
				.flatMap(broker -> {
					String neoFinKey = broker.getNeoFinKey();

					String orderData = String.format("{\"am\":\"%s\", \"on\":\"%s\"}", request.getAm(),
							request.getOn());
					String formData = "jData=" + orderData;

					return webClient.post().uri(exitBracketOrderUrl).header(HttpHeaders.ACCEPT, "application/json")
							.header("Sid", sid).header("Auth", step2).header("neo-fin-key", neoFinKey)
							.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + step1).bodyValue(formData).retrieve()
							.bodyToMono(String.class).map(response -> {
								try {
									JsonNode responseJson = objectMapper.readTree(response);
									Map<String, Object> result = new HashMap<>();
									result.put("cancelBracketResponse", responseJson);
									return result;
								} catch (Exception e) {
									logger.error("Error parsing cancel bracket order response", e);
									throw new BrokerApiException("Failed to parse cancel bracket order response", e);
								}
							});
				});
	}

	public Mono<Map<String, Object>> getLimits(LimitsDto request) {
		logger.info("Fetching limits for broker");

		String accessBrokerToken = request.getAccessBrokerToken();
		Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

		String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
		String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
		String sid = brokerAccessToken.get("sid").toString().trim();
		String userId = brokerAccessToken.get("userId").toString();

		return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
				.switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
				.flatMap(broker -> {
					String neoFinKey = broker.getNeoFinKey();

					String limitsData = String.format("{\"seg\":\"%s\", \"exch\":\"%s\", \"prod\":\"%s\"}",
							request.getSeg(), request.getExch(), request.getProd());
					String formData = "jData=" + limitsData;

					return webClient.post().uri(limitsUrl).header(HttpHeaders.ACCEPT, "application/json")
							.header("Sid", sid).header("Auth", step2).header("neo-fin-key", neoFinKey)
							.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + step1).bodyValue(formData).retrieve()
							.bodyToMono(String.class).map(response -> {
								try {
									JsonNode responseJson = objectMapper.readTree(response);
									Map<String, Object> result = new HashMap<>();
									result.put("limits", responseJson);
									return result;
								} catch (Exception e) {
									logger.error("Error parsing limits response", e);
									throw new BrokerApiException("Failed to parse limits response", e);
								}
							});
				});
	}

	public Mono<Map<String, Object>> checkMargin(MarginDto request) {
	    logger.info("Checking margin for broker");

	    String accessBrokerToken = request.getAccessBrokerToken();
	    Map<String, Object> brokerAccessToken = jwtUtil.decodeBrokerAccessToken(accessBrokerToken);

	    String step1 = brokerAccessToken.get("kotakTokenStep1").toString();
	    String step2 = brokerAccessToken.get("kotakTokenStep2").toString();
	    String sid = brokerAccessToken.get("sid").toString().trim();
	    String userId = brokerAccessToken.get("userId").toString();
	    
	    String serverId = brokerAccessToken.containsKey("sId") ? 
	                     brokerAccessToken.get("sId").toString().trim() : "";

	    return brokerRepository.findByUserIdAndBrokerCode(userId, request.getBrokerCode().toUpperCase().trim())
	            .switchIfEmpty(Mono.error(new RuntimeException("Broker not found")))
	            .flatMap(broker -> {
	                String neoFinKey = broker.getNeoFinKey();

	                // Create margin request object
	                Map<String, Object> marginRequest = new HashMap<>();
	                marginRequest.put("brkName", request.getBrkName());
	                marginRequest.put("brnchId", request.getBrnchId());
	                marginRequest.put("exSeg", request.getExSeg());
	                marginRequest.put("prc", request.getPrc());
	                marginRequest.put("prcTp", request.getPrcTp());
	                marginRequest.put("prod", request.getProd());
	                marginRequest.put("qty", request.getQty());
	                marginRequest.put("tok", request.getTok());
	                marginRequest.put("trnsTp", request.getTrnsTp());

	                // Add optional fields
	                if (request.getSlAbsOrTks() != null && !request.getSlAbsOrTks().isEmpty()) {
	                    marginRequest.put("slAbsOrTks", request.getSlAbsOrTks());
	                }
	                if (request.getSlVal() != null && !request.getSlVal().isEmpty()) {
	                    marginRequest.put("slVal", request.getSlVal());
	                }
	                if (request.getSqrOffAbsOrTks() != null && !request.getSqrOffAbsOrTks().isEmpty()) {
	                    marginRequest.put("sqrOffAbsOrTks", request.getSqrOffAbsOrTks());
	                }
	                if (request.getSqrOffVal() != null && !request.getSqrOffVal().isEmpty()) {
	                    marginRequest.put("sqrOffVal", request.getSqrOffVal());
	                }
	                if (request.getTrailSL() != null && !request.getTrailSL().isEmpty()) {
	                    marginRequest.put("trailSL", request.getTrailSL());
	                }
	                if (request.getTrgPrc() != null && !request.getTrgPrc().isEmpty()) {
	                    marginRequest.put("trgPrc", request.getTrgPrc());
	                }
	                if (request.getTSLTks() != null && !request.getTSLTks().isEmpty()) {
	                    marginRequest.put("tSLTks", request.getTSLTks());
	                }

	                try {
	                    String marginData = objectMapper.writeValueAsString(marginRequest);
	                    String formData = "jData=" + marginData;
	                    
	                    String urlWithQuery = checkMarginUrl + "?sId=" + serverId;

	                    // ADD DEBUG LOGGING
	                    logger.info("Margin request URL: {}", urlWithQuery);
	                    logger.info("Margin request headers - Sid: {}, Auth: {}, neo-fin-key: {}, Authorization: Bearer {}", 
	                               sid, step2, neoFinKey, step1);
	                    logger.info("Margin request body: {}", formData);

	                    return webClient.post()
	                            .uri(urlWithQuery)
	                            .header(HttpHeaders.ACCEPT, "application/json")
	                            .header("Sid", sid)
	                            .header("Auth", step2)
	                            .header("neo-fin-key", neoFinKey)
	                            .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
	                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + step1)
	                            .bodyValue(formData)
	                            .retrieve()
	                            .onStatus(status -> status.is4xxClientError(), response -> {
	                                return response.bodyToMono(String.class)
	                                        .map(errorBody -> {
	                                            logger.error("Margin check failed with status: {}, body: {}", 
	                                                       response.statusCode(), errorBody);
	                                            return new RuntimeException("Margin check failed: " + errorBody);
	                                        });
	                            })
	                            .bodyToMono(String.class)
	                            .map(response -> {
	                                try {
	                                    JsonNode responseJson = objectMapper.readTree(response);
	                                    Map<String, Object> result = new HashMap<>();
	                                    result.put("margin", responseJson);
	                                    return result;
	                                } catch (Exception e) {
	                                    logger.error("Error parsing margin response", e);
	                                    throw new BrokerApiException("Failed to parse margin response", e);
	                                }
	                            });
	                } catch (Exception e) {
	                    logger.error("Error creating margin request", e);
	                    return Mono.error(new BrokerApiException("Failed to create margin request", e));
	                }
	            });
	}

}