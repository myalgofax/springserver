package com.myalgofax.security.util.jwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtDecoder {
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public static String getSubClaim(String jwtToken) {
	    try {
	        String[] parts = jwtToken.split("\\.");
	        if (parts.length < 2) {
	            throw new IllegalArgumentException("Invalid JWT format");
	        }

	        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
	        JsonNode payloadNode = objectMapper.readTree(payload);

	        JsonNode subNode = payloadNode.get("sub");
	        if (subNode == null || subNode.asText().isEmpty()) {
	            throw new IllegalArgumentException("'sub' claim not found in JWT");
	        }

	        return subNode.asText().trim();

	    } catch (IllegalArgumentException | IOException e) {
	        throw new RuntimeException("Failed to extract 'sub' claim from JWT", e);
	    }
	}


	public static DecodedJwt decodeToken(String token) {
		// Remove "Bearer " prefix if present
		String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;

		// Split the JWT into parts
		String[] parts = jwt.split("\\.");
		if (parts.length != 3) {
			throw new IllegalArgumentException("Invalid JWT format");
		}

		return new DecodedJwt(parts[0], // header
				parts[1], // payload
				parts[2] // signature
		);
	}

	public static Claims decodeAndParse(String token) {
		String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
		return Jwts.parserBuilder().build().parseClaimsJws(jwt).getBody();
	}

	public static class DecodedJwt {
		private final String header;
		private final String payload;
		private final String signature;

		public DecodedJwt(String header, String payload, String signature) {
			this.header = header;
			this.payload = payload;
			this.signature = signature;
		}

		// Getters
		public String getHeader() {
			try {
				return new String(Base64.getUrlDecoder().decode(header), StandardCharsets.UTF_8);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Invalid JWT header encoding", e);
			}
		}

		public String getPayload() {
			try {
				return new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Invalid JWT payload encoding", e);
			}
		}

		public String getSignature() {
			return signature;
		}
	}
}
