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
package skills.controller

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.*
import skills.auth.AuthMode
import skills.controller.exceptions.SkillException
import skills.controller.exceptions.SkillsValidator
import skills.controller.request.model.GlobalSettingsRequest
import skills.controller.result.model.RequestResult
import skills.controller.result.model.UserInfoRes
import skills.controller.result.model.UserRoleRes
import skills.services.AccessSettingsStorageService
import skills.services.settings.SettingsService
import skills.settings.EmailConnectionInfo
import skills.settings.EmailSettingsService
import skills.storage.model.auth.RoleName

import java.security.Principal

@RestController
@RequestMapping('/root')
@Slf4j
@skills.profile.EnableCallStackProf
class RootController {

    @Autowired
    AccessSettingsStorageService accessSettingsStorageService

    @Autowired
    EmailSettingsService emailSettingsService

    @Value('#{securityConfig.authMode}}')
    skills.auth.AuthMode authMode = skills.auth.AuthMode.DEFAULT_AUTH_MODE

    @Autowired(required = false)
    skills.auth.pki.PkiUserLookup pkiUserLookup

    @Autowired
    UserDetailsService userDetailsService

    @Autowired
    SettingsService settingsService

    @GetMapping('/rootUsers')
    @ResponseBody
    List<UserRoleRes> getRootUsers() {
        return accessSettingsStorageService.getRootUsers()
    }

    @GetMapping('/users/{query}')
    @ResponseBody
    List<UserInfoRes> getNonRootUsers(@PathVariable('query') String query) {
        query = query.toLowerCase()
        if (authMode == AuthMode.FORM) {
            return accessSettingsStorageService.getNonRootUsers().findAll {
                it.userId.toLowerCase().contains(query)
            }.collect {
                accessSettingsStorageService.loadUserInfo(it.userId)
            }.unique()
        } else {
            List<String> rootUsers = accessSettingsStorageService.rootUsers.collect { it.userId.toLowerCase() }
            return pkiUserLookup?.suggestUsers(query)?.findAll {
                !rootUsers.contains(it.username.toLowerCase())
            }?.unique()?.take(5)?.collect { new UserInfoRes(it) }
        }
    }

    @GetMapping('/users/without/role/{roleName}/{query}')
    @ResponseBody
    List<UserInfoRes> suggestUsersWithoutRole(@PathVariable("roleName") RoleName roleName,
                                              @PathVariable('query') String query,
                                              @RequestParam(required = false, value = "userSuggestOption") String userSuggestOption) {
        query = query.toLowerCase()
        if (authMode == AuthMode.FORM) {
            return accessSettingsStorageService.getUserRolesWithoutRole(roleName).findAll {
                it.userId.toLowerCase().contains(query)
            }.collect {
                accessSettingsStorageService.loadUserInfo(it.userId)
            }.unique()
        } else {
            List<String> usersWithRole = accessSettingsStorageService.getUserRolesWithRole(roleName).collect { it.userId.toLowerCase() }
            return pkiUserLookup?.suggestUsers(query, userSuggestOption)?.findAll {
                !usersWithRole.contains(it.username.toLowerCase())
            }?.unique()?.take(5)?.collect { new UserInfoRes(it) }
        }
    }

    @GetMapping('/users/without/role/{roleName}')
    @ResponseBody
    List<UserInfoRes> suggestUsersWithoutRole(@PathVariable("roleName") RoleName roleName,
                                                     @RequestParam(required = false, value = "userSuggestOption") String userSuggestOption) {
        if (authMode == AuthMode.FORM) {
            return accessSettingsStorageService.getUserRolesWithoutRole(roleName).collect {
                accessSettingsStorageService.loadUserInfo(it.userId)
            }.unique()?.take(5)
        } else {
            List<String> usersWithRole = accessSettingsStorageService.getUserRolesWithRole(roleName).collect { it.userId.toLowerCase() }
            return pkiUserLookup?.suggestUsers("a", userSuggestOption)?.findAll {
                !usersWithRole.contains(it.username.toLowerCase())
            }?.unique()?.take(5)?.collect { new UserInfoRes(it) }
        }
    }

    @GetMapping('/isRoot')
    boolean isRoot(Principal principal) {
        return accessSettingsStorageService.isRoot(principal.name)
    }

    @PutMapping('/addRoot/{userKey}')
    RequestResult addRoot(@PathVariable('userKey') String userKey) {
        String userId = getUserId(userKey)
        accessSettingsStorageService.addRoot(userId)
        return new RequestResult(success: true)
    }

    @DeleteMapping('/deleteRoot/{userId}')
    void deleteRoot(@PathVariable('userId') String userId) {
        SkillsValidator.isTrue(accessSettingsStorageService.getRootAdminCount() > 1, 'At least one root user must exist at all times! Deleting another user will cause no root users to exist!')
        accessSettingsStorageService.deleteRoot(userId?.toLowerCase())
    }

    @GetMapping('/users/roles/{roleName}')
    @ResponseBody
    List<UserRoleRes> getUserRolesWithRole(@PathVariable("roleName") RoleName roleName) {
        return accessSettingsStorageService.getUserRolesWithRole(roleName)
    }

    @PutMapping('/users/{userKey}/roles/{roleName}')
    RequestResult addRole(@PathVariable('userKey') String userKey,
                          @PathVariable("roleName") RoleName roleName) {
        if (roleName == RoleName.ROLE_SUPER_DUPER_USER) {
            addRoot(userKey)
        } else {
            String userId = getUserId(userKey)
            accessSettingsStorageService.addUserRole(userId, null, roleName)
        }
        return new RequestResult(success: true)
    }

    @DeleteMapping('/users/{userId}/roles/{roleName}')
    void deleteRole(@PathVariable('userId') String userId,
                    @PathVariable("roleName") RoleName roleName) {
        userId = userId?.toLowerCase()
        if (roleName == RoleName.ROLE_SUPER_DUPER_USER) {
            deleteRoot(userId)
        } else {
            accessSettingsStorageService.deleteUserRole(userId, null, roleName)
        }
    }

    @PostMapping('/testEmailSettings')
    boolean testEmailSettings(@RequestBody EmailConnectionInfo emailConnectionInfo) {
        return emailSettingsService.testConnection(emailConnectionInfo)
    }

    @PostMapping('/saveEmailSettings')
    void saveEmailSettings(@RequestBody EmailConnectionInfo emailConnectionInfo) {
        emailSettingsService.updateConnectionInfo(emailConnectionInfo)
    }

    @RequestMapping(value = "/global/settings/{setting}", method = [RequestMethod.PUT, RequestMethod.POST], produces = MediaType.APPLICATION_JSON_VALUE)
    RequestResult saveGlobalSetting(@PathVariable("setting") String setting, @RequestBody GlobalSettingsRequest settingRequest) {
        SkillsValidator.isNotBlank(setting, "Setting Id")
        SkillsValidator.isTrue(setting == settingRequest.setting, "Setting Id must equal")

        settingsService.saveSetting(settingRequest)
        return new RequestResult(success: true)
    }

    private String getUserId(String userKey) {
        // userKey will be the userId when in FORM authMode, or the DN when in PKI auth mode.
        // When in PKI auth mode, the userDetailsService implementation will create the user
        // account if the user is not already a portal user (PkiUserDetailsService).
        // In the case of FORM authMode, the userKey is the userId and the user is expected
        // to already have a portal user account in the database
        if (authMode == AuthMode.PKI) {
            try {
                return userDetailsService.loadUserByUsername(userKey).username
            } catch (UsernameNotFoundException e) {
                throw new SkillException("User [$userKey] does not exist")
            }
        } else {
            return userKey
        }
    }
}
