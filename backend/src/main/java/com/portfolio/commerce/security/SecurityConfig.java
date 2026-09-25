package com.portfolio.commerce.security;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean JwtEncoder jwtEncoder(@Value("${auth.jwt-secret}") String secret){validateSecret(secret);return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secret.getBytes(StandardCharsets.UTF_8)));}
 @Bean JwtDecoder jwtDecoder(@Value("${auth.jwt-secret}") String secret){validateSecret(secret);return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256")).build();}
 private static void validateSecret(String secret){if(secret==null||secret.getBytes(StandardCharsets.UTF_8).length<32)throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");}
 @Bean JwtAuthenticationConverter jwtAuthenticationConverter(){JwtGrantedAuthoritiesConverter roles=new JwtGrantedAuthoritiesConverter();roles.setAuthoritiesClaimName("roles");roles.setAuthorityPrefix("ROLE_");JwtAuthenticationConverter converter=new JwtAuthenticationConverter();converter.setJwtGrantedAuthoritiesConverter(roles);return converter;}
 @Bean CorsConfigurationSource corsConfigurationSource(@Value("${cors.allowed-origins:http://localhost:14200}") String origins){CorsConfiguration config=new CorsConfiguration();config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s->!s.isEmpty()).toList());config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));config.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));config.setMaxAge(3600L);UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/api/**",config);return source;}
 @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtAuthenticationConverter converter)throws Exception{
  return http.csrf(csrf->csrf.disable()).cors(Customizer.withDefaults()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a
    .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
    .requestMatchers(HttpMethod.GET,"/api/products","/api/products/**").permitAll()
    .requestMatchers(HttpMethod.POST,"/api/auth/register","/api/auth/login").permitAll()
    .requestMatchers(HttpMethod.GET,"/api/auth/me").authenticated()
    .requestMatchers("/actuator/health").permitAll()
    .requestMatchers("/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**").permitAll()
    .requestMatchers("/api/products/**").hasRole("ADMIN")
    .requestMatchers("/api/carts/**").authenticated()
    .anyRequest().denyAll())
   .oauth2ResourceServer(o->o.jwt(j->j.jwtAuthenticationConverter(converter)))
   .build();
 }
}
