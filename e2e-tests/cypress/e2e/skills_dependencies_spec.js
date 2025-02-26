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

        cy.intercept({
            method: 'POST',
            path: '/admin/projects/proj1/skills/skill1/dependency/*',
        }, {
            statusCode: 400,
            body: {errorCode: 'FailedToAssignDependency', explanation: 'Error Adding Dependency'}
        }).as('addDependencyError');

        cy.intercept({
            method: 'GET',
            path: '/admin/projects/proj1/subjects/subj1/skills/skill1'
        }).as('loadSkill');

        cy.visit('/administrator/projects/proj1/subjects/subj1/skills/skill1');
        cy.wait('@loadSkill')

        cy.get('div#menu-collapse-control li').contains('Dependencies').click();

        cy.get('[data-cy="depsSelector"]').click();
        cy.get('[data-cy="depsSelector"] .vs__dropdown-option').eq(0).click();

        cy.wait('@addDependencyError')
        cy.get('div .alert').contains('Error! Request could not be completed! Error Adding Dependency');
    })

    it('do not allow circular dependencies', () => {
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

        cy.request('POST', '/admin/projects/proj1/skills/skill1/dependency/skill2')

        cy.visit('/administrator/projects/proj1/subjects/subj1/skills/skill2/dependencies');
        cy.contains('No Dependencies Yet');

        cy.get('[data-cy="depsSelector"]').click().type('Skill 1{enter}')

        cy.contains('Error! Request could not be completed! Discovered circular dependency [proj1:skill2 -> proj1:skill1 -> proj1:skill2]');
    });

    it('add dependencies - then remove via table', () => {
        const numSkills = 5;
        for (let i = 0; i < numSkills; i+=1) {
            cy.request('POST', `/admin/projects/proj1/subjects/subj1/skills/skill${i}`, {
                projectId: 'proj1',
                subjectId: "subj1",
                skillId: `skill${i}`,
                name: `Skill ${i}`,
                pointIncrement: '50',
                numPerformToCompletion: '5'
            });
        }

        cy.visit('/administrator/projects/proj1/subjects/subj1/skills/skill1/dependencies');
        cy.contains('No Dependencies Yet');

        cy.get('[data-cy="depsSelector"]').click().type('Skill 2{enter}');

        const tableSelector = '[data-cy="simpleSkillsTable"]';
        cy.validateTable(tableSelector, [
            [{ colIndex: 0,  value: 'Skill 2' }, { colIndex: 1,  value: 'skill2' }],
        ], 5);


        cy.get('[data-cy="depsSelector"]').click().type('Skill 3{enter}');
        cy.validateTable(tableSelector, [
            [{ colIndex: 0,  value: 'Skill 2' }, { colIndex: 1,  value: 'skill2' }],
            [{ colIndex: 0,  value: 'Skill 3' }, { colIndex: 1,  value: 'skill3' }],
        ], 5);

        cy.get('[data-cy="deleteSkill_skill2"]').click();
        cy.contains('Yes, Please').click();
        cy.validateTable(tableSelector, [
            [{ colIndex: 0,  value: 'Skill 3' }, { colIndex: 1,  value: 'skill3' }],
        ], 5);

        cy.get('[data-cy="deleteSkill_skill3"]').click();
        cy.contains('Yes, Please').click();
        cy.contains('No Dependencies Yet');
    });


    it('remove dependency after navigating directly to the page', () => {
        const tableSelector = '[data-cy="simpleSkillsTable"]';

        const numSkills = 5;
        for (let i = 0; i < numSkills; i += 1) {
            cy.request('POST', `/admin/projects/proj1/subjects/subj1/skills/skill${i}`, {
                projectId: 'proj1',
                subjectId: "subj1",
                skillId: `skill${i}`,
                name: `Skill ${i}`,
                pointIncrement: '50',
                numPerformToCompletion: '5'
            });
        }

        cy.request('POST', '/admin/projects/proj1/skills/skill1/dependency/skill2')
        cy.request('POST', '/admin/projects/proj1/skills/skill1/dependency/skill3')
        cy.request('POST', '/admin/projects/proj1/skills/skill1/dependency/skill4')

        cy.visit('/administrator/projects/proj1/subjects/subj1/skills/skill1/dependencies');
        cy.get(`${tableSelector} th`).contains('Skill ID').click();
        cy.validateTable(tableSelector, [
            [{ colIndex: 1,  value: 'skill2' }],
            [{ colIndex: 1,  value: 'skill3' }],
            [{ colIndex: 1,  value: 'skill4' }],
        ], 5);

        cy.get('[data-cy="deleteSkill_skill3"]').click();
        cy.contains('Yes, Please').click();
        cy.validateTable(tableSelector, [
            [{ colIndex: 1,  value: 'skill2' }],
            [{ colIndex: 1,  value: 'skill4' }],
        ], 5);
    });

    it('ability to navigate to skills that this skill depends on', () => {
        cy.createSubject(1, 2);
        cy.createSkill(1, 1, 1);
        cy.createSkill(1, 2, 2);
        cy.createSkill(1, 2, 3);
        cy.createSkill(1, 2, 4);
        cy.createSkill(1, 2, 5);

        cy.request('POST', '/admin/projects/proj1/skills/skill1/dependency/skill2Subj2');
        cy.request('POST', '/admin/projects/proj1/skills/skill1/dependency/skill3Subj2');
        cy.request('POST', '/admin/projects/proj1/skills/skill1/dependency/skill4Subj2');

        cy.visit('/administrator/projects/proj1/subjects/subj1/skills/skill1/dependencies');
        cy.get('[data-cy="manage_skill3Subj2"]')
            .click();
        cy.get('[data-cy="pageHeader"]')
            .contains('ID: skill3Subj2');
        cy.get('[data-cy="breadcrumb-subj2"]');
    });

    it('long skill id in the table', () => {
        const longId = 'eafeafeafeafeSkill%2DdlajleajljelajelkajlajleeafeafeafeafeSkill%2DdlajleajljelajelkajlajleeafeafeafeafeSkill%2Ddlajleajljelajelkajlajle'
        cy.intercept('GET', '/admin/projects/proj1/skills/skill1/dependency/graph', (req) => {
            req.reply({
                body: {
                    'nodes': [{
                        'id': 1,
                        'name': 'Skill 1',
                        'skillId': 'skill1',
                        'projectId': 'proj1',
                        'pointIncrement': 50,
                        'totalPoints': 250,
                        'type': 'Skill'
                    }, {
                        'id': 2,
                        'name': 'Skill 2',
                        'skillId': longId,
                        'projectId': 'proj1',
                        'pointIncrement': 50,
                        'totalPoints': 250,
                        'type': 'Skill'
                    }, {
                        'id': 3,
                        'name': 'Skill 3',
                        'skillId': 'skill3',
                        'projectId': 'proj1',
                        'pointIncrement': 50,
                        'totalPoints': 250,
                        'type': 'Skill'
                    }, {
                        'id': 4,
                        'name': 'Skill 4',
                        'skillId': 'skill4',
                        'projectId': 'proj1',
                        'pointIncrement': 50,
                        'totalPoints': 250,
                        'type': 'Skill'
                    }],
                    'edges': [{
                        'fromId': 1,
                        'toId': 2
                    }, {
                        'fromId': 1,
                        'toId': 3
                    }, {
                        'fromId': 1,
                        'toId': 4
                    }]
                },
            });
        }).as('getGraph');
        const tableSelector = '[data-cy="simpleSkillsTable"]';

        const numSkills = 5;
        for (let i = 0; i < numSkills; i += 1) {
            cy.request('POST', `/admin/projects/proj1/subjects/subj1/skills/skill${i}`, {
                projectId: 'proj1',
                subjectId: "subj1",
                skillId: `skill${i}`,
                name: `Skill ${i}`,
                pointIncrement: '50',
                numPerformToCompletion: '5'
            });
        }

        cy.request('POST', '/admin/projects/proj1/skills/skill1/dependency/skill2')
        cy.request('POST', '/admin/projects/proj1/skills/skill1/dependency/skill3')
        cy.request('POST', '/admin/projects/proj1/skills/skill1/dependency/skill4')

        cy.visit('/administrator/projects/proj1/subjects/subj1/skills/skill1/dependencies');
        cy.wait('@getGraph');
        cy.contains('eafeafeafeafeSkill%2Ddlajleajljelajelkaj... >> more');
    });

});
