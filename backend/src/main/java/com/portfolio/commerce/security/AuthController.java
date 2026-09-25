package com.portfolio.commerce.security;
import com.portfolio.commerce.domain.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final AuthAccountRepository accounts;private final UserRepository users;private final PasswordEncoder encoder;private final JwtEncoder jwt;
 public AuthController(AuthAccountRepository accounts,UserRepository users,PasswordEncoder encoder,JwtEncoder jwt){this.accounts=accounts;this.users=users;this.encoder=encoder;this.jwt=jwt;}
 public record RegisterRequest(@NotBlank @Email String email,@NotBlank @Size(min=12,max=72) String password,@NotBlank String dni,@NotBlank String firstName,@NotBlank String lastName){}
 public record LoginRequest(@NotBlank String email,@NotBlank String password){}
 public record AuthResponse(String token,Long userId,String email,String role){}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) @Transactional
 public AuthResponse register(@Valid @RequestBody RegisterRequest r){String email=r.email().trim().toLowerCase(Locale.ROOT);
  if(accounts.existsByEmailIgnoreCase(email)||users.findByDni(r.dni()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Email or DNI already registered");
  User user=users.save(new User(r.dni().trim(),r.firstName().trim(),r.lastName().trim(),false));
  AuthAccount account=accounts.save(new AuthAccount(user,email,encoder.encode(r.password()),Role.USER));return issue(account);
 }
 @PostMapping("/login")
 public AuthResponse login(@Valid @RequestBody LoginRequest r){AuthAccount account=accounts.findByEmailIgnoreCase(r.email().trim()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials"));
  if(!encoder.matches(r.password(),account.getPasswordHash()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");return issue(account);
 }
 @GetMapping("/me")
 public Map<String,Object> me(Authentication auth){AuthAccount account=accounts.findByEmailIgnoreCase(auth.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));return Map.of("userId",account.getUser().getId(),"email",account.getEmail(),"role",account.getRole().name());}
 private AuthResponse issue(AuthAccount a){Instant now=Instant.now();JwtClaimsSet claims=JwtClaimsSet.builder().issuer("commerce-platform").subject(a.getEmail()).issuedAt(now).expiresAt(now.plus(Duration.ofHours(2))).claim("userId",a.getUser().getId()).claim("roles",List.of(a.getRole().name())).build();String token=jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();return new AuthResponse(token,a.getUser().getId(),a.getEmail(),a.getRole().name());}
}
