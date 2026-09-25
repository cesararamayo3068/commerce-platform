package com.portfolio.commerce.security;
import com.portfolio.commerce.domain.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.Locale;
@Configuration
public class AdminBootstrap {
 @Bean ApplicationRunner bootstrapAdmin(@Value("${auth.admin.email:}") String email,@Value("${auth.admin.password:}") String password,@Value("${auth.admin.dni:}") String dni,AuthAccountRepository accounts,UserRepository users,PasswordEncoder encoder,TransactionTemplate transactions){
  return args->{if(email.isBlank()&&password.isBlank()&&dni.isBlank())return;
   if(email.isBlank()||password.length()<12||dni.isBlank())throw new IllegalStateException("ADMIN_EMAIL, ADMIN_PASSWORD (12+ chars) and ADMIN_DNI must all be configured");
   transactions.executeWithoutResult(tx->{String normalized=email.trim().toLowerCase(Locale.ROOT);
    if(accounts.existsByEmailIgnoreCase(normalized))return;
    User user=users.findByDni(dni).orElseGet(()->users.save(new User(dni,"Commerce","Administrator",false)));
    accounts.save(new AuthAccount(user,normalized,encoder.encode(password),Role.ADMIN));
   });
  };
 }
}
