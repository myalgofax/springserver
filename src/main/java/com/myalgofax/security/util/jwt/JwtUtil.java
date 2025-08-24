package com.myalgofax.security.util.jwt;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration:86400000}") // default to 1 day
	private long expiration;

	private Key key;
	private static final ObjectMapper mapper = new ObjectMapper();

	@PostConstruct
	public void init() {
		byte[] keyBytes = Base64.getEncoder().encode(secret.getBytes());
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(String email, String userId) {
		return Jwts.builder().claim("userId", userId).setSubject(email).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public String generateBrokerAccessToken(String kotakTokenStep1, String kotakTokenStep2, String sid, String userId,
			String brokerCode) {
		return Jwts.builder()
				.claim("sid", sid).claim("brokerCode", brokerCode).claim("userId", userId)
				.claim("kotakTokenStep2", kotakTokenStep2).claim("kotakTokenStep1", kotakTokenStep1)
				.setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public Map<String, Object> decodeBrokerAccessToken(String jwt) {
		logger.debug("Decoding broker access token");
		Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
		Map<String, Object> brokerJwtToken = decodeBrokerJwtToken(claims.get("kotakTokenStep1", String.class));

		Map<String, Object> extracted = new HashMap<>();
		extracted.put("sid", claims.get("sid", String.class).trim());
		extracted.put("userId", claims.get("userId", String.class));
		extracted.put("kotakTokenStep2", claims.get("kotakTokenStep2", String.class));
		extracted.put("kotakTokenStep1", claims.get("kotakTokenStep1", String.class));
		extracted.put("brokerCode", claims.get("brokerCode", String.class));
		extracted.put("issuedAt", claims.getIssuedAt());
		extracted.put("expiresAt", claims.getExpiration());

		extracted.put("brokerJwtToken", brokerJwtToken);
		return extracted;
	}

	public static Map<String, Object> decodeBrokerJwtToken(String jwtToken) {
		try {
			String[] parts = jwtToken.split("\\.");
			if (parts.length < 2) {
				throw new IllegalArgumentException("Invalid JWT token format.");
			}

			String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
			return mapper.readValue(payload, new TypeReference<>() {
			});
		} catch (Exception e) {
			throw new RuntimeException("Failed to decode JWT", e);
		}
	}

	public String extractEmail(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
	}

	public String extractUserId(String token) {
		try {
			return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("userId",
					String.class);
		} catch (JwtException e) {
			logger.debug("Failed to extract userId from token", e);
			return null;
		}
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException e) {
			logger.debug("Token validation failed", e);
			return false;
		}
	}

}
