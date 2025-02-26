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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletRequest
import java.util.regex.Matcher
import java.util.regex.Pattern

@Slf4j
@CompileStatic
class AuthUtils {
    static final Pattern PROJECT_ID_PATTERN = Pattern.compile("/\\S+?/projects/([^/]+).*\$")

    // Example: /admin/projects/{projectId}/approvals/approve
    // Example: /admin/projects/{projectId}/approvals/reject
    static final Pattern PROJECT_SELF_REPORT_APPROVE_OR_REJECT_PATTERN = Pattern.compile("^/admin/projects/[^/]+/approvals/(?:approve|reject)\$")

    // Example: /admin/projects/{projectId}/approverConf
    static final Pattern PROJECT_SELF_REPORT_APPROVER_CONF_PATTERN = Pattern.compile("^/admin/projects/[^/]+/approverConf\$")

    // Example: /projects/{projectId}/approvalEmails/unsubscribe
    static final Pattern PROJECT_SELF_REPORT_EMAIL_UNSUB_CONF_PATTERN = Pattern.compile("^/admin/projects/[^/]+/approvalEmails/(?:unsubscribe|subscribe)\$")

    static String getProjectIdFromRequest(HttpServletRequest servletRequest) {
        String projectId
        if (servletRequest) {
            String servletPath = servletRequest.getServletPath()
            Matcher matcher = PROJECT_ID_PATTERN.matcher(servletPath)
            if (matcher.matches()) {
                if (matcher.hasGroup()) {
                    projectId = matcher.group(1)
                } else {
                    log.warn("no projectId found for endpoint [$servletPath]?")
                }
            }
        }
        return projectId
    }

    static boolean isSelfReportApproveOrRejectEndpoint(HttpServletRequest servletRequest) {
        if (servletRequest) {
            String servletPath = servletRequest.getServletPath()
            return PROJECT_SELF_REPORT_APPROVE_OR_REJECT_PATTERN.matcher(servletPath).matches()
        }
        return false
    }

    static boolean isSelfReportApproverConfEndpoint(HttpServletRequest servletRequest) {
        if (servletRequest) {
            String servletPath = servletRequest.getServletPath()
            return PROJECT_SELF_REPORT_APPROVER_CONF_PATTERN.matcher(servletPath).matches()
        }
        return false
    }

    static boolean isSelfReportEmailSubscriptionEndpoint(HttpServletRequest servletRequest) {
        if (servletRequest) {
            String servletPath = servletRequest.getServletPath()
            return PROJECT_SELF_REPORT_EMAIL_UNSUB_CONF_PATTERN.matcher(servletPath).matches()
        }
        return false
    }



}
