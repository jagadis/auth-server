package com.acme.demo.auth.user

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import java.security.Principal

/**
 * Return information about the currently logged in user
 *
 * @author William Gorder
 * @since 1/10/15
 */
@RestController
@Slf4j
class UserController {

    @Autowired
    UserDetailsService userDetailsService

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public AcmeUserDetails user(Principal principal) {
        if(principal instanceof OAuth2Authentication) {
           return userDetailsService.loadUserByUsername(principal.getUserAuthentication().getName())
        }
        else if (principal instanceof AcmeUserDetails) {
            return principal
        }
        return null
    }
}
