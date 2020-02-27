/**
 * Copyright 2020 SkillTree
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package skills.auth.form

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import skills.PublicProps
import skills.auth.SecurityMode
import skills.controller.PublicPropsBasedValidator
import skills.storage.model.auth.RoleName
import skills.storage.model.auth.UserRole

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

//@Conditional(SecurityConfiguration.FormAuth)
@RestController
@RequestMapping("/")
@Slf4j
class CreateAccountController {

    @Autowired
    skills.auth.UserAuthService userAuthService

    @Autowired
    PasswordEncoder passwordEncoder

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired(required = false)
    OAuth2ProviderProperties oAuth2ProviderProperties

    @Autowired
    PublicPropsBasedValidator propsBasedValidator

    @Conditional(SecurityMode.FormAuth)
    @PutMapping("createAccount")
    void createAppUser(@RequestBody skills.auth.UserInfo userInfo, HttpServletResponse response) {
        String password = userInfo.password
        propsBasedValidator.validateMinStrLength(PublicProps.UiProp.minPasswordLength, "password", password)
        propsBasedValidator.validateMaxStrLength(PublicProps.UiProp.maxPasswordLength, "password", password)

        userInfo.password = passwordEncoder.encode(password)
        if (!userInfo.username) {
            userInfo.username = userInfo.email
        }
        if (!userInfo.nickname) {
            userInfo.nickname = "${userInfo.firstName} ${userInfo.lastName}"
        }
        userInfo.usernameForDisplay = userInfo.username
        userInfo = userAuthService.createUser(userInfo)
        userAuthService.autologin(userInfo, password)
    }

    @Conditional(SecurityMode.FormAuth)
    @PutMapping("createRootAccount")
    void createRootUser(@RequestBody skills.auth.UserInfo userInfo, HttpServletResponse response) {
        skills.controller.exceptions.SkillsValidator.isTrue(!userAuthService.rootExists(), 'A root user already exists! Granting additional root privileges requires a root user to grant them!')
        String password = userInfo.password
        propsBasedValidator.validateMinStrLength(PublicProps.UiProp.minPasswordLength, "password", password)
        propsBasedValidator.validateMaxStrLength(PublicProps.UiProp.maxPasswordLength, "password", password)

        userInfo.password = passwordEncoder.encode(password)
        if (!userInfo.username) {
            userInfo.username = userInfo.email
        }
        if (!userInfo.nickname) {
            userInfo.nickname = "${userInfo.firstName} ${userInfo.lastName}".toString()
        }
        if (!userInfo.usernameForDisplay) {
            userInfo.usernameForDisplay = userInfo.username
        }
        userInfo.authorities = [new skills.auth.UserSkillsGrantedAuthority(new UserRole(
                roleName: RoleName.ROLE_SUPER_DUPER_USER
        ))]
        userAuthService.createUser(userInfo, true)
    }

    @Conditional(SecurityMode.PkiAuth)
    @RequestMapping(value = "/grantFirstRoot", method = [RequestMethod.POST, RequestMethod.PUT])
    void grantFirstRoot(HttpServletRequest request) {
        skills.controller.exceptions.SkillsValidator.isTrue(!userAuthService.rootExists(), 'A root user already exists! Granting additional root privileges requires a root user to grant them!')
        skills.controller.exceptions.SkillsValidator.isNotNull(request.getUserPrincipal(), 'Granting the first root user is only available in PKI mode, but it looks like the request was not made by an authenticated account!')
        userAuthService.grantRoot(request.getUserPrincipal().name)
    }

    @Conditional(SecurityMode.FormAuth)
    @GetMapping('userExists/{user}')
    boolean userExists(@PathVariable('user') String user) {
        return userAuthService.userExists(user)
    }

    @Conditional(SecurityMode.FormAuth)
    @GetMapping("/app/oAuthProviders")
    List<skills.controller.result.model.OAuth2Provider> getOAuthProviders() {
        List<skills.controller.result.model.OAuth2Provider> providers = []
        if (clientRegistrationRepository && oAuth2ProviderProperties) {
            clientRegistrationRepository.iterator().each { ClientRegistration clientRegistration ->
                skills.controller.result.model.OAuth2Provider oAuth2Provider = oAuth2ProviderProperties.registration.get(clientRegistration.registrationId)
                oAuth2Provider.registrationId = oAuth2Provider.registrationId ?: clientRegistration.registrationId
                oAuth2Provider.clientName = oAuth2Provider.clientName ?: clientRegistration.clientName
                providers.add(oAuth2Provider)
            }
        }
        return providers
    }

    @Component
    @Configuration
    @ConfigurationProperties(prefix = "spring.security.oauth2.client")
    @Conditional(SecurityMode.FormAuth)
    static class OAuth2ProviderProperties {
        final Map<String, skills.controller.result.model.OAuth2Provider> registration = new HashMap<>()
    }
}
