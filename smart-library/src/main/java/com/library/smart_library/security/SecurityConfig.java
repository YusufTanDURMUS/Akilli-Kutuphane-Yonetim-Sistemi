package com.library.smart_library.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF İptal
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 👇 CORS AYARINI AKTİF ET

                .authorizeHttpRequests(auth -> auth
                        // 1. STATİK DOSYALAR (Resim, CSS, JS, Favicon)
                        .requestMatchers("/", "/index.html", "/books.html", "/reset-password.html",
                                "/verify-email.html", "/favicon.ico", "/*.css", "/*.js",
                                "/images/**")
                        .permitAll()

                        // 2. GİRİŞ, KAYIT ve HATA SAYFALARI (ÖNEMLİ: /error EKLENDİ!)
                        .requestMatchers("/api/v1/auth/**", "/error").permitAll()

                        // 2.1 İNTERNAL ENDPOİNTLER (Sadece Geliştirme için - Production'da kapatın!)
                        .requestMatchers("/api/v1/internal/**").permitAll()

                        // 3. API TEST (GET istekleri serbest olsun ki kitaplar görünsün)
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/**", "/api/v1/borrow/**").permitAll()

                        // 4. KİTAP EKLEME (POST) -> Token İster
                        .requestMatchers(HttpMethod.POST, "/api/v1/books/**").authenticated()

                        // 5. KİTAP DÜZENLEME VE SİLME (PUT, DELETE) -> Token İster
                        .requestMatchers(HttpMethod.PUT, "/api/v1/books/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**").authenticated()

                        // 6. BORROW POST İŞLEMLERİ (Ödünç alma) -> Token İster
                        .requestMatchers("/api/v1/borrow/**").authenticated()

                        // 7. KULLANICI YÖNETİMİ -> Token İster
                        .requestMatchers("/api/v1/users/**").authenticated()

                        // Diğer her şey için Token şart
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 👇 TARAYICI ENGELİNİ KALDIRAN CORS AYARI
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Hangi adreslerden istek gelebilir? (Hepsine izin veriyoruz *)
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @SuppressWarnings("deprecation")
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}