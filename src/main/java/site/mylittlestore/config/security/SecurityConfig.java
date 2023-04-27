package site.mylittlestore.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import site.mylittlestore.config.auth.PrincipalUserDetailsService;
import site.mylittlestore.config.auth.oauth.OAuth2LoginFailureHandler;
import site.mylittlestore.config.auth.oauth.OAuth2LoginSuccessHandler;
import site.mylittlestore.config.auth.oauth.OAuth2LogoutHandler;
import site.mylittlestore.config.auth.oauth.PrincipalOAuth2UserService;
import site.mylittlestore.enumstorage.role.MemberRole;
import site.mylittlestore.filter.auth.AuthenticationProcessFilter;
import site.mylittlestore.filter.auth.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity(debug = true) //Spring Securty 필터가 Spring Filter Chain에 등록된다.
//@EnableGlobalMethodSecurity(securedEnabled = true) //secured 어노테이션 활성화
@RequiredArgsConstructor
public class SecurityConfig {
    private final PrincipalUserDetailsService principalUserDetailsService;
    private final PrincipalOAuth2UserService principalOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final OAuth2LogoutHandler OAuth2LogoutHandler;
    private final AuthenticationProcessFilter authenticationProcessFilter;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors()
            .and()
                .addFilter(corsConfig.corsFilter());

        http
                // 기본 페이지, css, image, js 하위 폴더에 있는 자료들은 모두 접근 가능, h2-console에 접근 가능
                .authorizeHttpRequests(authorize -> authorize
//                        .antMatchers("/members/**").hasAuthority(MemberRole.ADMIN.toString())
                        .antMatchers("/","/css/**","/images/**","/js/**","/favicon.ico","/h2-console/**").permitAll()
                        .antMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                );

        http
                //세션
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //세션을 사용하지 않겠다.
            .and()
                .httpBasic() //http header에 username, password를 넣어서 전송하는 방법을
                .disable(); //해제

        //Filter
        http
                .addFilterAfter(authenticationProcessFilter, LogoutFilter.class)
                .addFilterAfter(jwtAuthenticationFilter(), AuthenticationProcessFilter.class);
//                .addFilterAfter(jwtAuthorizationFilter, JwtAuthenticationFilter.class);

//        //로그인
//        http
//                .formLogin()
//                .loginPage("/auth/login")
//                .loginProcessingUrl("/auth/login")
//                .usernameParameter("email")
//                .defaultSuccessUrl("/", true)
//                .failureUrl("/auth/login?error")
////                .failureHandler(new SimpleUrlAuthenticationFailureHandler("/auth/login?error"))
//                .permitAll()
//            .and()
//                //로그아웃
//                .logout()
//                .logoutUrl("/auth/logout")
//                .invalidateHttpSession(true)
//                .deleteCookies("JSESSIONID");
////                .exceptionHandling();

        //OAuth2 로그인
        http
                .oauth2Login()
                .loginPage("/auth/login")
                .defaultSuccessUrl("/", true)
                .authorizationEndpoint()
                .baseUri("/auth/login/oauth2/authorization")
            .and()
                .redirectionEndpoint()
                .baseUri("/auth/login/oauth2/code/*")
            .and()
                .userInfoEndpoint()
                .userService(principalOAuth2UserService)
            .and()
                .successHandler(oAuth2LoginSuccessHandler) // 동의하고 계속하기를 눌렀을 때 Handler 설정(
                .failureHandler(oAuth2LoginFailureHandler) // 소셜 로그인 실패 시 핸들러 설정
            .and()
                .logout()
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login")
                .addLogoutHandler(OAuth2LogoutHandler);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(customAuthenticationProvider);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(authenticationManager());
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        //이 부분에서 큰 권한 순서로 ' > ' 를 사용하여 입력해준다. 띄어쓰기도 중요하다.
        roleHierarchy.setHierarchy(MemberRole.ADMIN + " > " + MemberRole.MEMBER);

        return roleHierarchy;
    }
}
