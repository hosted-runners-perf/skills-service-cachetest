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
package skills.auth

import groovy.util.logging.Slf4j
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.access.AccessDecisionManager
import org.springframework.security.access.vote.UnanimousBased
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler
import org.springframework.security.web.access.expression.WebExpressionVoter
import skills.auth.inviteOnly.InviteOnlyProjectAccessDecisionVoter

@Slf4j
@Configuration
@Order(99)
class CustomAccessDecisionManager {

    @Bean
    AccessDecisionManager accessDecisionManager(InviteOnlyProjectAccessDecisionVoter inviteOnlyProjectAccessDecisionVoter) {
        OAuth2WebSecurityExpressionHandler oauth2ExpressionHandler = new OAuth2WebSecurityExpressionHandler()
        WebExpressionVoter expressionVoter = new WebExpressionVoter()
        expressionVoter.setExpressionHandler(oauth2ExpressionHandler)
        assert inviteOnlyProjectAccessDecisionVoter != null
        return new UnanimousBased([expressionVoter, inviteOnlyProjectAccessDecisionVoter])
    }
}
