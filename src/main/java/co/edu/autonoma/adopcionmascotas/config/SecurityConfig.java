package co.edu.autonoma.adopcionmascotas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// usuarios de juguete para el taller: en un proyecto real van en la base de datos
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService usuarios(PasswordEncoder encoder) {
        UserDetails ana = User.withUsername("ana")
                .password(encoder.encode("ana123"))
                .roles("SOLICITANTE")
                .build();
        UserDetails laura = User.withUsername("laura")
                .password(encoder.encode("laura123"))
                .roles("COORDINADOR")
                .build();
        UserDetails fundacion = User.withUsername("fundacion")
                .password(encoder.encode("fundacion123"))
                .roles("FUNDACION")
                .build();
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(ana, laura, fundacion, admin);
    }

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
                // una API REST no usa el formulario con cookies de sesion
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // la pagina de error debe ser publica para que un 404 no se vuelva 401
                        .requestMatchers("/error").permitAll()
                        // catalogo publico
                        .requestMatchers(HttpMethod.GET, "/api/mascotas", "/api/mascotas/**").permitAll()
                        // el solicitante no tiene cuenta: crear la solicitud es publico
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes-adopcion").permitAll()
                        // la accion mas peligrosa: borrar una mascota
                        .requestMatchers(HttpMethod.DELETE, "/api/mascotas", "/api/mascotas/**").hasRole("ADMIN")
                        // todo lo demas exige usuario y contrasena
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
