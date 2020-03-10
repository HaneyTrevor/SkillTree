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
package skills.intTests.clientDisplay

import skills.intTests.utils.DefaultIntSpec
import skills.intTests.utils.SkillsFactory
import skills.intTests.utils.SkillsService

class SkillsDescriptionSpec extends DefaultIntSpec {

    def "result should be empty if there are not descriptions at all"(){
        def proj1 = SkillsFactory.createProject(1)
        def proj1_subj1 = SkillsFactory.createSubject(1, 1)
        def proj1_subj2 = SkillsFactory.createSubject(1, 2)
        List<Map> proj1_subj1_skills = SkillsFactory.createSkills(3, 1, 1)
        List<Map> proj1_subj2_skills = SkillsFactory.createSkills(3, 1, 2)

        proj1_subj2_skills.each {
            it.description = null
            it.helpUrl = null
        }

        skillsService.createProject(proj1)
        skillsService.createSubject(proj1_subj1)
        skillsService.createSkills(proj1_subj1_skills)
        skillsService.createSubject(proj1_subj2)
        skillsService.createSkills(proj1_subj2_skills)

        when:
        def res = skillsService.getSubjectDescriptions(proj1.projectId, proj1_subj2.subjectId)
        then:
        res.each {
            assert !it.description
            assert !it.href
        }
    }

    def "subject has no skills - no descriptions for you!"(){
        def proj1 = SkillsFactory.createProject(1)
        def proj1_subj1 = SkillsFactory.createSubject(1, 1)
        def proj1_subj2 = SkillsFactory.createSubject(1, 2)
        List<Map> proj1_subj1_skills = SkillsFactory.createSkills(3, 1, 1)

        skillsService.createProject(proj1)
        skillsService.createSubject(proj1_subj1)
        skillsService.createSkills(proj1_subj1_skills)
        skillsService.createSubject(proj1_subj2)

        when:
        def res = skillsService.getSubjectDescriptions(proj1.projectId, proj1_subj2.subjectId)
        then:
        !res
    }

    def "get descriptions for a subject"(){
        def proj1 = SkillsFactory.createProject(1)
        def proj1_subj1 = SkillsFactory.createSubject(1, 1)
        def proj1_subj2 = SkillsFactory.createSubject(1, 2)
        List<Map> proj1_subj1_skills = SkillsFactory.createSkills(3, 1, 1)
        List<Map> proj1_subj2_skills = SkillsFactory.createSkills(3, 1, 2)

        proj1_subj2_skills.each {
            it.description = "Desc [${it.skillId}]".toString()
            it.helpUrl = "http://${it.skillId}".toString()
        }

        proj1_subj2_skills[1].description = null

        skillsService.createProject(proj1)
        skillsService.createSubject(proj1_subj1)
        skillsService.createSkills(proj1_subj1_skills)
        skillsService.createSubject(proj1_subj2)
        skillsService.createSkills(proj1_subj2_skills)

        when:
        def res = skillsService.getSubjectDescriptions(proj1.projectId, proj1_subj2.subjectId).sort { it.skillId }
        then:
        res[0].description == "Desc [${proj1_subj2_skills[0].skillId}]".toString()
        res[0].href == "http://${proj1_subj2_skills[0].skillId}".toString()

        !res[1].description
        res[1].href == "http://${proj1_subj2_skills[1].skillId}".toString()

        res[2].description == "Desc [${proj1_subj2_skills[2].skillId}]".toString()
        res[2].href == "http://${proj1_subj2_skills[2].skillId}".toString()
    }

    def "descriptions should respect root url settings"(){
        def proj1 = SkillsFactory.createProject(1)
        def proj1_subj1 = SkillsFactory.createSubject(1, 1)
        List<Map> proj1_subj1_skills = SkillsFactory.createSkills(3, 1, 1)

        proj1_subj1_skills.each {
            it.description = "Desc [${it.skillId}]".toString()
            it.helpUrl = "/${it.skillId}".toString()
        }


        skillsService.createProject(proj1)
        skillsService.changeSetting(proj1.projectId, "help.url.root", [projectId: proj1.projectId, setting: 'help.url.root', value:"https://fakeurl.foo"])
        skillsService.createSubject(proj1_subj1)
        skillsService.createSkills(proj1_subj1_skills)

        when:
        def res = skillsService.getSubjectDescriptions(proj1.projectId, proj1_subj1.subjectId).sort { it.skillId }
        then:
        res.each {
            assert it.href.startsWith("https://fakeurl.foo")
        }
    }

    def "badge has no skills - no descriptions for you!"(){
        def proj1 = SkillsFactory.createProject(1)
        def proj1_subj1 = SkillsFactory.createSubject(1, 1)
        def proj1_subj2 = SkillsFactory.createSubject(1, 2)
        List<Map> proj1_subj1_skills = SkillsFactory.createSkills(3, 1, 1)

        Map badge1 = SkillsFactory.createBadge(1, 1 )

        skillsService.createProject(proj1)
        skillsService.createSubject(proj1_subj1)
        skillsService.createSkills(proj1_subj1_skills)
        skillsService.createSubject(proj1_subj2)
        skillsService.createBadge(badge1)

        when:
        def res = skillsService.getBadgeDescriptions(proj1.projectId, badge1.badgeId)
        then:
        !res
    }

    def "global badge has no skills - no descriptions for you!"() {
        SkillsService supervisorService = createSupervisor()

        def proj1 = SkillsFactory.createProject(1)
        def proj1_subj1 = SkillsFactory.createSubject(1, 1)
        def proj1_subj2 = SkillsFactory.createSubject(1, 2)
        List<Map> proj1_subj1_skills = SkillsFactory.createSkills(3, 1, 1)

        Map badge1 = SkillsFactory.createBadge(1, 1)

        skillsService.createProject(proj1)
        skillsService.createSubject(proj1_subj1)
        skillsService.createSkills(proj1_subj1_skills)
        skillsService.createSubject(proj1_subj2)
        supervisorService.createGlobalBadge(badge1)

        when:
        def res = skillsService.getBadgeDescriptions(proj1.projectId, badge1.badgeId, true)
        then:
        !res
    }

    def "get descriptions for a badge"(){
        def proj1 = SkillsFactory.createProject(1)
        def proj1_subj1 = SkillsFactory.createSubject(1, 1)
        def proj1_subj2 = SkillsFactory.createSubject(1, 2)
        List<Map> proj1_subj1_skills = SkillsFactory.createSkills(3, 1, 1)
        List<Map> proj1_subj2_skills = SkillsFactory.createSkills(3, 1, 2)

        proj1_subj2_skills.each {
            it.description = "Desc [${it.skillId}]".toString()
            it.helpUrl = "http://${it.skillId}".toString()
        }

        proj1_subj2_skills[1].description = null

        Map badge1 = SkillsFactory.createBadge(1, 1 )
        Map badge2 = SkillsFactory.createBadge(1, 2)

        skillsService.createProject(proj1)
        skillsService.createSubject(proj1_subj1)
        skillsService.createSkills(proj1_subj1_skills)
        skillsService.createSubject(proj1_subj2)
        skillsService.createSkills(proj1_subj2_skills)

        skillsService.createBadge(badge1)
        skillsService.createBadge(badge2)

        proj1_subj1_skills.each {
            skillsService.assignSkillToBadge(proj1.projectId, badge1.badgeId, it.skillId)
        }

        proj1_subj2_skills.each {
            skillsService.assignSkillToBadge(proj1.projectId, badge2.badgeId, it.skillId)
        }

        when:
        def res = skillsService.getBadgeDescriptions(proj1.projectId, badge2.badgeId).sort { it.skillId }
        then:
        res[0].description == "Desc [${proj1_subj2_skills[0].skillId}]".toString()
        res[0].href == "http://${proj1_subj2_skills[0].skillId}".toString()

        !res[1].description
        res[1].href == "http://${proj1_subj2_skills[1].skillId}".toString()

        res[2].description == "Desc [${proj1_subj2_skills[2].skillId}]".toString()
        res[2].href == "http://${proj1_subj2_skills[2].skillId}".toString()
    }

    def "get descriptions for a global badge"(){
        SkillsService supervisorService = createSupervisor()

        def proj1 = SkillsFactory.createProject(1)
        def proj1_subj1 = SkillsFactory.createSubject(1, 1)
        def proj1_subj2 = SkillsFactory.createSubject(1, 2)
        List<Map> proj1_subj1_skills = SkillsFactory.createSkills(3, 1, 1)
        List<Map> proj1_subj2_skills = SkillsFactory.createSkills(3, 1, 2)

        proj1_subj2_skills.each {
            it.description = "Desc [${it.skillId}]".toString()
            it.helpUrl = "http://${it.skillId}".toString()
        }

        proj1_subj2_skills[1].description = null

        Map badge1 = SkillsFactory.createBadge(1, 1 )
        Map badge2 = SkillsFactory.createBadge(1, 2)

        skillsService.createProject(proj1)
        skillsService.createSubject(proj1_subj1)
        skillsService.createSkills(proj1_subj1_skills)
        skillsService.createSubject(proj1_subj2)
        skillsService.createSkills(proj1_subj2_skills)

        supervisorService.createGlobalBadge(badge1)
        supervisorService.createGlobalBadge(badge2)

        proj1_subj1_skills.each {
            supervisorService.assignSkillToGlobalBadge([projectId: proj1.projectId, badgeId: badge1.badgeId, skillId: it.skillId])
        }

        proj1_subj2_skills.each {
            supervisorService.assignSkillToGlobalBadge([projectId: proj1.projectId, badgeId: badge2.badgeId, skillId: it.skillId])
        }

        when:
        def res = skillsService.getBadgeDescriptions(proj1.projectId, badge2.badgeId, true).sort { it.skillId }
        then:
        res[0].description == "Desc [${proj1_subj2_skills[0].skillId}]".toString()
        res[0].href == "http://${proj1_subj2_skills[0].skillId}".toString()

        !res[1].description
        res[1].href == "http://${proj1_subj2_skills[1].skillId}".toString()

        res[2].description == "Desc [${proj1_subj2_skills[2].skillId}]".toString()
        res[2].href == "http://${proj1_subj2_skills[2].skillId}".toString()
    }


    def "badge's skills have no descriptions"(){
        def proj1 = SkillsFactory.createProject(1)
        def proj1_subj1 = SkillsFactory.createSubject(1, 1)
        def proj1_subj2 = SkillsFactory.createSubject(1, 2)
        List<Map> proj1_subj1_skills = SkillsFactory.createSkills(3, 1, 1)
        List<Map> proj1_subj2_skills = SkillsFactory.createSkills(3, 1, 2)

        proj1_subj2_skills.each {
            it.description = null
            it.helpUrl = null
        }

        proj1_subj2_skills[1].description = null

        Map badge1 = SkillsFactory.createBadge(1, 1 )
        Map badge2 = SkillsFactory.createBadge(1, 2)

        skillsService.createProject(proj1)
        skillsService.createSubject(proj1_subj1)
        skillsService.createSkills(proj1_subj1_skills)
        skillsService.createSubject(proj1_subj2)
        skillsService.createSkills(proj1_subj2_skills)

        skillsService.createBadge(badge1)
        skillsService.createBadge(badge2)

        proj1_subj1_skills.each {
            skillsService.assignSkillToBadge(proj1.projectId, badge1.badgeId, it.skillId)
        }

        proj1_subj2_skills.each {
            skillsService.assignSkillToBadge(proj1.projectId, badge2.badgeId, it.skillId)
        }

        when:
        def res = skillsService.getBadgeDescriptions(proj1.projectId, badge2.badgeId)
        then:
        res.each {
            assert !it.description
            assert !it.href
        }
    }
}
