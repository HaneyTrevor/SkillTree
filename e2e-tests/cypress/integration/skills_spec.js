/*
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
describe('Skills Tests', () => {

    beforeEach(() => {
        cy.request('POST', '/app/projects/proj1', {
            projectId: 'proj1',
            name: "proj1"
        })
        cy.request('POST', '/admin/projects/proj1/subjects/subj1', {
            projectId: 'proj1',
            subjectId: 'subj1',
            name: "Subject 1"
        })
    });

    it('edit number of occurrences', () => {
        cy.server()
        cy.route('POST', `/admin/projects/proj1/subjects/subj1/skills/Skill1Skill`).as('postNewSkill');
        cy.route('GET', `/admin/projects/proj1/subjects/subj1/skills/Skill1Skill`).as('getSkill');
        cy.route({
            method: 'GET',
            url: '/admin/projects/proj1/subjects/subj1'
        }).as('loadSubject');

        const selectorOccurrencesToCompletion = '[data-vv-name="numPerformToCompletion"]';
        const selectorSkillsRowToggle = 'table .VueTables__child-row-toggler';
        cy.visit('/projects/proj1/subjects/subj1');

        cy.wait('@loadSubject');

        cy.clickButton('Skill')
        cy.get(selectorOccurrencesToCompletion).should('have.value', '5')
        cy.get('#skillName').type('Skill 1')

        cy.clickSave()
        cy.wait('@postNewSkill');


        cy.get(selectorSkillsRowToggle).click()
        cy.contains('50 Points')

        cy.get('table .control-column .fa-edit').click()
        cy.wait('@getSkill')

        // close toast
        cy.get('.toast-header button').click({ multiple: true })
        cy.get(selectorOccurrencesToCompletion).should('have.value', '5')
        cy.get(selectorOccurrencesToCompletion).type('{backspace}10')
        cy.get(selectorOccurrencesToCompletion).should('have.value', '10')

        cy.clickSave()
        cy.wait('@postNewSkill');

        cy.get(selectorSkillsRowToggle).click()
        cy.contains('100 Points')
    });

    it('create skill with special chars', () => {
        const expectedId = 'LotsofspecialPcharsSkill';
        const providedName = "!L@o#t$s of %s^p&e*c(i)a_l++_|}{P c'ha'rs";
        cy.server().route('POST', `/admin/projects/proj1/subjects/subj1/skills/${expectedId}`).as('postNewSkill');

        cy.route({
            method: 'GET',
            url: '/admin/projects/proj1/subjects/subj1'
        }).as('loadSubject');

        cy.visit('/projects/proj1/subjects/subj1');
        cy.wait('@loadSubject');
        cy.clickButton('Skill')

        cy.get('#skillName').type(providedName)

        cy.getIdField().should('have.value', expectedId)

        cy.clickSave()
        cy.wait('@postNewSkill');

        cy.contains('ID: Lotsofspecial')
    });

    it('Add Skill Event', () => {
        cy.request('POST', '/admin/projects/proj1/subjects/subj1/skills/skill1', {
            projectId: 'proj1',
            subjectId: "subj1",
            skillId: "skill1",
            name: "Skill 1",
            pointIncrement: '50',
            numPerformToCompletion: '5'
        });

        cy.server();
        cy.route({
            method: 'POST',
            url: '/app/users/projects/proj1/suggestClientUsers?userSuggestOption=TWO'
        }).as('suggestUsers');
        cy.route({
            method: 'GET',
            url: '/admin/projects/proj1/subjects/subj1/skills/skill1'
        }).as('loadSkill');
        cy.route({
            method: 'POST',
            url: '/api/projects/Inception/skills/ManuallyAddSkillEvent'
        }).as('addSkillEvent');

       cy.visit('/projects/proj1/subjects/subj1/skills/skill1');
       cy.wait('@loadSkill');
       cy.contains('Add Event').click();

       cy.contains('ONE').click();
       cy.contains('TWO').click();
       cy.get('.existingUserInput button').contains('TWO');

       cy.contains('Enter user id').type('foo{enter}');
       cy.wait('@suggestUsers');
       cy.clickButton('Add');
       cy.wait('@addSkillEvent');
       cy.get('.text-success', {timeout: 5*1000}).contains('Added points for');
       cy.get('.text-success', {timeout: 5*1000}).contains('[foo]');

        cy.contains('Enter user id').type('bar{enter}');
        cy.wait('@suggestUsers');
        cy.clickButton('Add');
        cy.wait('@addSkillEvent');
        cy.get('.text-success', {timeout: 5*1000}).contains('Added points for');
        cy.get('.text-success', {timeout: 5*1000}).contains('[bar]');

        cy.contains('Enter user id').type('baz{enter}');
        cy.wait('@suggestUsers');
        cy.clickButton('Add');
        cy.wait('@addSkillEvent');
        cy.get('.text-success', {timeout: 5*1000}).contains('Added points for');
        cy.get('.text-success', {timeout: 5*1000}).contains('[baz]');

        cy.contains('Enter user id').type('fo');
        cy.wait('@suggestUsers');
        cy.get('li.multiselect__element').contains('foo').click();
    });

    it('Add Skill Event - suggest user with slash character does not cause error', () => {
        cy.request('POST', '/admin/projects/proj1/subjects/subj1/skills/skill1', {
            projectId: 'proj1',
            subjectId: "subj1",
            skillId: "skill1",
            name: "Skill 1",
            pointIncrement: '50',
            numPerformToCompletion: '5'
        });

        cy.server();
        cy.route({
            method: 'POST',
            url: '/app/users/projects/proj1/suggestClientUsers?userSuggestOption=TWO'
        }).as('suggestUsers');
        cy.route({
            method: 'GET',
            url: '/admin/projects/proj1/subjects/subj1/skills/skill1'
        }).as('loadSkill');

        cy.visit('/projects/proj1/subjects/subj1/skills/skill1');
        cy.wait('@loadSkill');
        cy.contains('Add Event').click();

        cy.contains('ONE').click();
        cy.contains('TWO').click();
        cy.get('.existingUserInput button').contains('TWO');

        cy.contains('Enter user id').type('foo/bar{enter}');
        cy.wait('@suggestUsers');
    });

    it('Add Skill Event User Not Found', () => {
       cy.server();
       cy.route({
           method: 'PUT',
           url: '/api/projects/*/skills/*',
           status: 400,
           response: {errorCode: 'UserNotFound', explanation: 'Some Error Occurred'}
       }).as('addUser');

        cy.request('POST', '/admin/projects/proj1/subjects/subj1/skills/skill1', {
            projectId: 'proj1',
            subjectId: "subj1",
            skillId: "skill1",
            name: "Skill 1",
            pointIncrement: '50',
            numPerformToCompletion: '5'
        });

        cy.route({
            method: 'GET',
            url: '/admin/projects/proj1/subjects/subj1/skills/skill1'
        }).as('loadSkill')

        cy.visit('/projects/proj1/subjects/subj1/skills/skill1');
        cy.wait('@loadSkill')


        cy.contains('Add Event').click();

        cy.contains('Enter user id').type('foo{enter}');

        cy.clickButton('Add');
        cy.wait('@addUser');
        cy.get('.text-danger', {timeout: 5*1000}).contains("Wasn't able to add points for");
    });

    it('Add Skill Event - user names cannot have spaces', () => {
        cy.request('POST', '/admin/projects/proj1/subjects/subj1/skills/skill1', {
            projectId: 'proj1',
            subjectId: "subj1",
            skillId: "skill1",
            name: "Skill 1",
            pointIncrement: '50',
            numPerformToCompletion: '5'
        });
        cy.server();
        cy.route({
            method: 'GET',
            url: '/admin/projects/proj1/subjects/subj1/skills/skill1'
        }).as('loadSkill');

        cy.visit('/projects/proj1/subjects/subj1/skills/skill1');
        cy.wait('@loadSkill');
        cy.contains('Add Event').click();

        const expectedErrMsg = 'The User Id field may not contain spaces';
        const userIdSelector = '[data-cy=userIdInput]';
        const addButtonSelector = '[data-cy=addSkillEventButton]';

        cy.get(userIdSelector).type('user a{enter}');
        cy.contains(expectedErrMsg)
        cy.get(addButtonSelector).should('be.disabled')

        cy.get(userIdSelector).type('userd{enter}');
        cy.contains(expectedErrMsg).should('not.exist');
        cy.get(addButtonSelector).should('not.be.disabled')

        cy.get(userIdSelector).type('user d{enter}');
        cy.contains(expectedErrMsg)
        cy.get(addButtonSelector).should('be.disabled')

        cy.get(userIdSelector).type('userOK{enter}');
        cy.contains(expectedErrMsg).should('not.exist');
        cy.get(addButtonSelector).should('not.be.disabled')
        cy.get(addButtonSelector).click();
        cy.contains('userOK');

        cy.get(userIdSelector).type('user@#$&*{enter}');
        cy.contains(expectedErrMsg).should('not.exist');
        cy.get(addButtonSelector).should('not.be.disabled')
        cy.get(addButtonSelector).click();
        cy.contains('user@#$&*');
    });

    it('Add Dependency failure', () => {
        cy.request('POST', '/admin/projects/proj1/subjects/subj1/skills/skill1', {
            projectId: 'proj1',
            subjectId: "subj1",
            skillId: "skill1",
            name: "Skill 1",
            pointIncrement: '50',
            numPerformToCompletion: '5'
        });

        cy.request('POST', '/admin/projects/proj1/subjects/subj1/skills/skill2', {
            projectId: 'proj1',
            subjectId: "subj1",
            skillId: "skill2",
            name: "Skill 2",
            pointIncrement: '50',
            numPerformToCompletion: '5'
        });

        cy.server();

        cy.route({
            method: 'POST',
            status: 400,
            url: '/admin/projects/proj1/skills/skill1/dependency/*',
            response: {errorCode: 'FailedToAssignDependency', explanation: 'Error Adding Dependency'}
        });

        cy.route({
            method: 'GET',
            url: '/admin/projects/proj1/subjects/subj1/skills/skill1'
        }).as('loadSkill');

        cy.visit('/projects/proj1/subjects/subj1/skills/skill1');
        cy.wait('@loadSkill')

        cy.get('div#menu-collapse-control li').contains('Dependencies').click();

        cy.get('.multiselect__tags').click();
        cy.get('.multiselect__tags input').type('{enter}')

        cy.get('div .alert').contains('Error! Request could not be completed! Error Adding Dependency');

    })

});
