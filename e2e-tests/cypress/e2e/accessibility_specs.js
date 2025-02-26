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
import dayjs from 'dayjs';

const moment = require('moment-timezone');

describe('Accessibility Tests', () => {

    /*after(() => {
      cy.task('createAverageAccessibilityScore');
    });*/

    beforeEach(() => {
        cy.request('POST', '/app/projects/MyNewtestProject', {
            projectId: 'MyNewtestProject',
            name: 'My New test Project'
        });

        cy.request('POST', '/app/projects/MyNewtestProject2', {
            projectId: 'MyNewtestProject2',
            name: 'My New test Project2'
        });

        cy.request('POST', '/admin/projects/MyNewtestProject/subjects/subj1', {
            projectId: 'MyNewtestProject',
            subjectId: 'subj1',
            name: 'Subject 1'
        });

        cy.request('POST', '/admin/projects/MyNewtestProject/badges/badge1', {
            projectId: 'MyNewtestProject',
            badgeId: 'badge1',
            name: 'Badge 1'
        });

        cy.request('POST', `/admin/projects/MyNewtestProject/subjects/subj1/skills/skill1`, {
            projectId: 'MyNewtestProject',
            subjectId: 'subj1',
            skillId: 'skill1',
            name: `This is 1`,
            type: 'Skill',
            pointIncrement: 100,
            numPerformToCompletion: 5,
            pointIncrementInterval: 0,
            numMaxOccurrencesIncrementInterval: -1,
            description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.',
            version: 0,
            helpUrl: 'http://doHelpOnThisSkill.com'
        });

        cy.request('POST', `/admin/projects/MyNewtestProject/subjects/subj1/skills/skill2`, {
            projectId: 'MyNewtestProject',
            subjectId: 'subj1',
            skillId: 'skill2',
            name: `This is 2`,
            type: 'Skill',
            pointIncrement: 100,
            numPerformToCompletion: 5,
            pointIncrementInterval: 0,
            numMaxOccurrencesIncrementInterval: -1,
            description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.',
            version: 0,
            helpUrl: 'http://doHelpOnThisSkill.com'
        });

        cy.request('POST', `/admin/projects/MyNewtestProject/subjects/subj1/skills/skill3`, {
            projectId: 'MyNewtestProject',
            subjectId: 'subj1',
            skillId: 'skill3',
            name: `This is 3`,
            type: 'Skill',
            pointIncrement: 100,
            numPerformToCompletion: 5,
            pointIncrementInterval: 0,
            numMaxOccurrencesIncrementInterval: -1,
            description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.',
            version: 0,
            helpUrl: 'http://doHelpOnThisSkill.com',
            selfReportingType: 'Approval'
        });
        cy.request('POST', `/admin/projects/MyNewtestProject/subjects/subj1/skills/skill4`, {
            projectId: 'MyNewtestProject',
            subjectId: 'subj1',
            skillId: 'skill4',
            name: `This is 4`,
            type: 'Skill',
            pointIncrement: 100,
            numPerformToCompletion: 5,
            pointIncrementInterval: 0,
            numMaxOccurrencesIncrementInterval: -1,
            description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.',
            version: 0,
            helpUrl: 'http://doHelpOnThisSkill.com',
        });

        cy.request('POST', '/admin/projects/MyNewtestProject/badge/badge1/skills/skill2');

        cy.request('POST', `/admin/projects/MyNewtestProject/skills/skill2/dependency/skill1`);

        const m = moment('2020-05-12 11', 'YYYY-MM-DD HH');
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill1`, {
            userId: 'u1',
            timestamp: m.format('x')
        });
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill1`, {
            userId: 'u2',
            timestamp: m.subtract(4, 'day')
                .format('x')
        });
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill1`, {
            userId: 'u3',
            timestamp: m.subtract(3, 'day')
                .format('x')
        });
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill1`, {
            userId: 'u4',
            timestamp: m.subtract(2, 'day')
                .format('x')
        });
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill1`, {
            userId: 'u5',
            timestamp: m.subtract(1, 'day')
                .format('x')
        });
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill3`, {
            userId: 'u5',
            timestamp: m.subtract(1, 'day')
                .format('x')
        });
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill3`, {
            userId: 'u6',
            timestamp: m.subtract(1, 'day')
                .format('x')
        });
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill3`, {
            userId: 'u7',
            timestamp: m.subtract(1, 'day')
                .format('x')
        });

        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill4`, {
            userId: 'u8',
            timestamp: m.subtract(1, 'day')
                .format('x')
        });
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill4`, {
            userId: 'u8',
            timestamp: m.subtract(2, 'day')
                .format('x')
        });
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill4`, {
            userId: 'u8',
            timestamp: m.subtract(3, 'day')
                .format('x')
        });
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill4`, {
            userId: 'u8',
            timestamp: m.subtract(4, 'day')
                .format('x')
        });
        cy.request('POST', `/api/projects/MyNewtestProject/skills/skill4`, {
            userId: 'u8',
            timestamp: m.subtract(5, 'day')
                .format('x')
        });
    });

    it('"My Progress" landing page', () => {
        // setup a project for the landing page
        const dateFormatter = value => moment.utc(value)
            .format('YYYY-MM-DD[T]HH:mm:ss[Z]');
        const timeFromNowFormatter = (value) => dayjs(value)
            .startOf('hour')
            .fromNow();
        cy.createProject(1);
        cy.enableProdMode(1);
        cy.addToMyProjects(1);
        cy.createSubject(1, 1);
        cy.createSubject(1, 2);
        cy.createSubject(1, 3);
        cy.createSkill(1, 1, 1);
        cy.createSkill(1, 1, 2);
        cy.createSkill(1, 1, 3);
        cy.createSkill(1, 1, 4);

        cy.request('POST', `/admin/projects/proj1/skills/skill4/dependency/skill2`);

        cy.request('POST', `/api/projects/proj1/skills/skill1`, {
            userId: Cypress.env('proxyUser'),
            timestamp: new Date().getTime()
        });
        cy.request('POST', `/api/projects/proj1/skills/skill1`, {
            userId: Cypress.env('proxyUser'),
            timestamp: new Date().getTime() - 1000 * 60 * 60 * 24
        });

        cy.request('POST', `/api/projects/proj1/skills/skill3`, {
            userId: Cypress.env('proxyUser'),
            timestamp: new Date().getTime()
        });
        cy.request('POST', `/api/projects/proj1/skills/skill3`, {
            userId: Cypress.env('proxyUser'),
            timestamp: new Date().getTime() - 1000 * 60 * 60 * 24
        });

        cy.request('POST', '/admin/projects/proj1/badges/badge1', {
            projectId: 'proj1',
            badgeId: 'badge1',
            name: 'Badge 1'
        });

        cy.request('POST', '/admin/projects/proj1/badges/gemBadge', {
            projectId: 'proj1',
            badgeId: 'gemBadge',
            name: 'Gem Badge',
            startDate: dateFormatter(new Date() - 1000 * 60 * 60 * 24 * 7),
            endDate: dateFormatter(new Date() + 1000 * 60 * 60 * 24 * 5),
        });

        cy.visit('/progress-and-rankings');
        cy.get('[data-cy="breadcrumb-Progress And Rankings"]')
            .contains('Progress And Rankings')
            .should('be.visible');
        cy.get('[data-cy=numSkillsAvailable]')
            .contains(new RegExp(/^Total: 4$/));
        cy.get('[data-cy=project-link-proj1]')
            .should('be.visible');

        cy.customLighthouse();
        cy.injectAxe();
        cy.customA11y();
    });

    it('admin home page', () => {
        cy.visit('/administrator/');
        cy.customLighthouse();
        cy.injectAxe();
        cy.get('[data-cy=nav-Projects]');
        cy.contains('My New test Project');
        cy.customA11y();
    });

    it('project', () => {
        //report skills that dont' exist
        cy.reportSkill('MyNewtestProject', 42, 'user@skills.org', '2021-02-24 10:00', false);
        cy.reportSkill('MyNewtestProject', 75, 'user@skills.org', '2021-02-24 10:00', false);
        cy.reportSkill('MyNewtestProject', 75, 'user@skills.org', '2021-02-24 10:00', false);
        cy.reportSkill('MyNewtestProject', 75, 'user@skills.org', '2021-02-24 10:00', false);
        cy.reportSkill('MyNewtestProject', 13, 'user@skills.org', '2021-02-24 10:00', false);

        cy.visit('/administrator/');
        cy.injectAxe();
        //view project
        cy.get('[data-cy=projCard_MyNewtestProject_manageBtn]')
            .click();
        // wait on subjects
        cy.get('[data-cy=manageBtn_subj1]');

        cy.customLighthouse();
        cy.get('[aria-label="new subject"]')
            .click();
        cy.get('[data-cy=subjectNameInput]')
            .type('a');
        cy.customA11y();
        cy.get('[data-cy=closeSubjectButton]')
            .click();

        cy.get('[data-cy=nav-Badges]')
            .click();
        cy.contains('Badge 1');

        cy.customLighthouse();
        cy.get('[aria-label="new badge"]')
            .click();
        cy.get('[data-cy=badgeName')
            .type('a');
        cy.customA11y();
        cy.get('[data-cy=closeBadgeButton]')
            .click();

        // --- Self Report Page ----
        cy.get('[data-cy="nav-Self Report"]')
            .click();
        cy.get('[data-cy="skillsReportApprovalTable"] tbody tr')
            .should('have.length', 3);
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy="selectPageOfApprovalsBtn"]')
            .click();
        cy.get('[data-cy="rejectBtn"]')
            .click();
        cy.get('[data-cy="rejectionTitle"]')
            .contains('This will reject user\'s request(s) to get points');
        cy.wait(500); // wait for modal to continue loading, if background doesn't load the contract checks will fail
        cy.customA11y();
        cy.get('[data-cy="cancelRejectionBtn"]')
            .click();

        // --- Deps Page ----
        cy.get('[data-cy=nav-Dependencies]')
            .click();
        cy.contains('Color Legend');
        cy.customLighthouse();
        cy.customA11y();

        //levels
        cy.get('[data-cy=nav-Levels')
            .click();
        // cy.contains('White Belt');
        cy.customLighthouse();
        cy.get('[data-cy=addLevel]')
            .click();
        cy.get('[data-cy=levelPercent]')
            .type('1100');
        cy.customA11y();
        cy.get('[data-cy=cancelLevel]')
            .click();

        //users
        cy.get('[data-cy=nav-Users')
            .click();
        cy.contains('ID: MyNewtestProject');
        cy.get('[data-cy="usersTable"]')
            .contains('u1');
        cy.contains('User Id Filter');
        cy.contains('Total Rows: 6');
        cy.customLighthouse();
        cy.customA11y();

        // --- metrics ----
        cy.get('[data-cy=nav-Metrics]')
            .click();
        cy.contains('Users per day');
        cy.contains('This chart needs at least 2 days of user activity.');
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy="Achievements-metrics-link"]')
            .click();
        cy.contains('Level 2: 1 users');
        cy.contains('Level 1: 0 users');
        cy.get('[data-cy=achievementsNavigator-table]')
            .contains('u8');
        cy.wait(2000); // wait for charts to finish loading
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy="Subjects-metrics-link"]')
            .click();
        cy.contains('Number of users for each level over time');
        cy.wait(4000); // wait for charts to finish loading
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy="Skills-metrics-link"]')
            .click();
        cy.get('[data-cy=skillsNavigator-table]')
            .contains('This is 1');
        cy.customLighthouse();
        cy.customA11y();

        // --- access page ----
        cy.get('[data-cy=nav-Access')
            .click();
        cy.contains('Trusted Client Properties');
        cy.contains('ID: MyNewtestProject');
        const tableSelector = '[data-cy="roleManagerTable"]';
        cy.get(tableSelector)
            .contains('Loading...')
            .should('not.exist');
        cy.get(tableSelector)
            .contains('There are no records to show')
            .should('not.exist');
        cy.get(`${tableSelector} tbody tr`)
            .should('have.length', 1);
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy=nav-Settings]')
            .click();
        cy.contains('Root Help Url');
        cy.customLighthouse();
        cy.customA11y();

        // --- Issues page ---
        cy.intercept('GET', '/admin/projects/MyNewtestProject/errors*')
            .as('getErrors');
        cy.get('[data-cy=nav-Issues]')
            .click();
        cy.wait('@getErrors');
        cy.contains('Remove All');
        cy.customLighthouse();
        cy.customA11y();
    });

    it('subject', () => {
        cy.server();
        cy.route('GET', '/admin/projects/MyNewtestProject/subjects')
            .as('getSubjects');
        cy.route('GET', '/admin/projects/MyNewtestProject/subjects/subj1/skills')
            .as('getSkills');

        cy.visit('/administrator/');
        cy.injectAxe();
        //view project
        cy.get('[data-cy=projCard_MyNewtestProject_manageBtn]')
            .click();
        cy.wait('@getSubjects');
        //view subject
        cy.get('[data-cy=manageBtn_subj1]')
            .click();
        cy.wait('@getSkills');
        cy.contains('This is 2');
        cy.customLighthouse();
        cy.customA11y();
    });

    it('subject - create skill', () => {
        cy.visit('/administrator/projects/MyNewtestProject/subjects/subj1');
        cy.injectAxe();

        cy.get('[aria-label="new skill"]')
            .click();
        cy.get('[data-cy=skillName]')
            .type('1');
        cy.contains('Skill Name cannot be less than 3 characters.');
        cy.customA11y();
    })

    it('subject - levels', () => {
        cy.visit('/administrator/projects/MyNewtestProject/subjects/subj1/levels');
        cy.injectAxe();

        cy.get('[data-cy="levelsTable"]').contains('67')
        // cy.contains('Black Belt');r
        cy.customLighthouse();
        cy.customA11y();
        cy.get('[data-cy=addLevel]')
            .click();
        cy.get('[data-cy=levelPercent]')
            .type('105');
        cy.contains('Percent % must be 100 or less');
        cy.customA11y();
        cy.get('[data-cy=cancelLevel]')
            .click();
        cy.get('[data-cy=editLevelButton]')
            .eq(0)
            .click();
        cy.get('[data-cy=levelPercent]')
            .type('ddddddddd');
        cy.contains('Percent may only contain numeric characters');
        cy.customA11y();
        cy.get('[data-cy=cancelLevel]')
            .click();
    })

    it('subject - users', () => {
        cy.visit('/administrator/projects/MyNewtestProject/subjects/subj1/users');
        cy.injectAxe();

        cy.get('[data-cy="skillsBTableTotalRows"]').should('have.text', '6')
        cy.contains('u1');
        cy.customLighthouse();
        cy.customA11y();
    })

    it('subject - user - client display', () => {
        cy.visit('/administrator/projects/MyNewtestProject/subjects/subj1/users/u1');
        cy.injectAxe();

        cy.contains('Client Display');
        cy.wait(4000);
        cy.customLighthouse();
        // enable once a11y issues with client display are addressed, needs an H1 initially
        // also has contrast issues
        // cy.customA11y();
    })

    it('subject - user - performed skills', () => {
        cy.visit('/administrator/projects/MyNewtestProject/subjects/subj1/users/u1/skillEvents');
        cy.injectAxe();
        cy.get('[data-cy="performedSkillsTable"]').contains('ID: skill1');
        cy.customLighthouse();
        cy.customA11y();
    })

    it('subject - metrics', () => {
        cy.visit('/administrator/projects/MyNewtestProject/subjects/subj1');
        cy.injectAxe();
        cy.get('[data-cy=nav-Metrics]')
            .click();
        cy.contains('This chart needs at least 2 days of user activity.');
        cy.contains('Level 2: 1 users');
        cy.customLighthouse();
        cy.customA11y();
    })

    it('skills', () => {
        cy.visit('/administrator/');
        cy.injectAxe();
        //view project
        cy.get('[data-cy="projCard_MyNewtestProject_manageBtn"]')
            .click();
        //view subject
        cy.get('[data-cy="manageBtn_subj1"]')
            .click();
        //view skill
        cy.get('[data-cy=manageSkillBtn_skill1]')
            .click();
        cy.contains('Help URL');
        cy.contains('http://doHelpOnThisSkill.com');
        cy.contains('Lorem ipsum dolor sit amet, consectetur adipiscing elit,');
        cy.contains('500 Points');
        cy.customLighthouse();
        cy.customA11y();

        cy.clickNav('Dependencies');
        cy.contains('No Dependencies Yet');
        cy.contains('You can manage and visualize skill\'s dependencies on this page');
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy=nav-Users]')
            .click();
        cy.contains('u1');
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy="nav-Add Event"]')
            .click();
        cy.customLighthouse();
        cy.customA11y();
        cy.get('[data-cy="userIdInput"]')
            .click();
        cy.get('[data-cy="userIdInput"] .vs__dropdown-option')
            .eq(0)
            .click();
        cy.get('[data-cy="eventDatePicker"]')
            .click();
        cy.get('.vdp-datepicker__calendar .prev')
            .first()
            .click();
        cy.get('.vdp-datepicker__calendar .prev')
            .first()
            .click();
        cy.get('.vdp-datepicker__calendar .prev')
            .first()
            .click();
        cy.get('.vdp-datepicker__calendar .prev')
            .first()
            .click();
        cy.get('.vdp-datepicker__calendar .prev')
            .first()
            .click();
        cy.get('.vdp-datepicker__calendar .prev')
            .first()
            .click();
        cy.get('.vdp-datepicker__calendar')
            .contains('10')
            .click();
        cy.get('[data-cy=addSkillEventButton]')
            .click();
        cy.contains('Added points');
        cy.customA11y();

        cy.get('[data-cy=nav-Metrics]')
            .click();
        cy.contains('Achievements over time');
        cy.get('[data-cy="numUsersPostAchievement"]')
            .contains('No achievements yet for this skill.');
        cy.get('[data-cy="numUsersPostAchievement"]')
            .contains('No achievements yet for this skill.');

        cy.contains('This chart needs at least 2 days of user activity.');
        cy.customLighthouse();
        cy.customA11y();
    });

    it('badges', () => {
        cy.visit('/administrator/');
        cy.injectAxe();

        cy.get('[data-cy="projCard_MyNewtestProject_manageBtn"]')
            .click();
        cy.clickNav('Badges')
            .click();

        cy.get('[aria-label="new badge"]')
            .click();
        cy.get('[data-cy=badgeName]')
            .type('a');
        cy.customA11y();
        cy.get('[data-cy=closeBadgeButton]')
            .click();

        cy.get('[data-cy=manageBtn_badge1]')
            .click();
        cy.contains('This is 2');
        cy.customLighthouse();
        cy.customA11y();

        cy.clickNav('Users')
            .click();
        cy.contains('There are no records to show');
        cy.customLighthouse();
        cy.customA11y();
    });

    it('settings', () => {
        cy.logout();
        cy.login('root@skills.org', 'password');
        cy.visit('/settings');
        cy.contains('First Name');
        cy.injectAxe();
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy="nav-Preferences"]')
            .click();
        cy.contains('Preferences');
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy=nav-Security]')
            .click();
        cy.contains('Root Users Management');
        cy.get('[data-cy="supervisorrm"]')
            .contains('There are no records to show');
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy=nav-Email]')
            .click();
        cy.contains('Email Connection Settings');
        cy.contains('TLS Disabled');
        cy.contains('Public URL');
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy=nav-System]')
            .click();
        cy.contains('Token Expiration');
        cy.customLighthouse();
        cy.customA11y();
    });

    it('global badges', () => {
        cy.logout();
        cy.login('root@skills.org', 'password');

        cy.intercept('POST', ' /supervisor/badges/globalbadgeBadge/projects/MyNewtestProject/level/1')
            .as('saveGlobalBadgeLevel');
        cy.request('PUT', `/root/users/root@skills.org/roles/ROLE_SUPERVISOR`);
        cy.visit('/administrator');
        cy.injectAxe();
        cy.get('[data-cy="nav-Global Badges"]')
            .click();
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[aria-label="new global badge"]')
            .click();
        cy.get('[data-cy=badgeName]')
            .type('global badge');
        cy.get('[data-cy=saveBadgeButton]')
            .click();
        cy.contains('Manage')
            .click();
        cy.customLighthouse();
        cy.customA11y();

        cy.get('[data-cy="skillsSelector2"]')
            .click();
        cy.contains('This is 1');
        cy.get('[data-cy="skillsSelector2"] .vs__dropdown-option')
            .eq(0)
            .click();
        cy.customA11y();
        cy.get('[data-cy=nav-Levels]')
            .click();
        cy.contains('No Levels Added Yet');
        cy.customLighthouse();

        cy.get('#project-selector')
            .click();
        cy.get('#project-selector .vs__dropdown-option')
            .eq(0)
            .click();
        cy.get('#level-selector')
            .click();
        cy.get('#level-selector .vs__dropdown-option')
            .eq(1)
            .click();
        // cy.get('.multiselect__select').eq(0).click();
        // cy.get('.multiselect__element').eq(0).click();
        // cy.get('.multiselect__select').eq(1).click();
        // cy.get('.multiselect__element').eq(1).click();

        cy.get('[data-cy=addGlobalBadgeLevel]')
            .click();
        cy.customA11y();
    });

    it('manage my projects page', () => {

        for (let i = 1; i <= 9; i += 1) {
            cy.createProject(i);
            cy.enableProdMode(i);
            if (i < 4) {
                cy.addToMyProjects(i);
            }
        }

        cy.visit('/progress-and-rankings');
        cy.injectAxe();
        cy.get('[data-cy="manageMyProjsBtn"]')
            .click();

        cy.get('[data-cy="allProjectsCount"] [data-cy="mediaInfoCardTitle"]')
            .contains('9');

        cy.wait(1500);
        cy.customLighthouse();
        cy.customA11y();
    });

    it('my usage page', () => {
        cy.fixture('vars.json')
            .then((vars) => {
                cy.request('POST', '/logout');
                cy.register(Cypress.env('proxyUser'), vars.defaultPass, false);
                cy.loginAsProxyUser();
            });
        cy.loginAsProxyUser();

        for (let i = 1; i <= 3; i += 1) {
            cy.createProject(i);
            cy.enableProdMode(i);
            cy.addToMyProjects(i);

            cy.createSubject(i, 1);
            cy.createSkill(i, 1, 1);
            cy.createSkill(i, 1, 2);
        }

        const dateFormat = 'YYYY-MM-DD HH:mm';
        const user = Cypress.env('proxyUser');
        cy.reportSkill(1, 1, user, moment.utc()
            .subtract(2, 'days')
            .format(dateFormat));
        cy.reportSkill(1, 1, user, 'yesterday');
        cy.reportSkill(1, 2, user, 'now');
        cy.reportSkill(1, 2, user, 'yesterday');

        cy.reportSkill(2, 1, user, moment.utc()
            .subtract(5, 'days')
            .format(dateFormat));
        cy.reportSkill(2, 1, user, moment.utc()
            .subtract(6, 'days')
            .format(dateFormat));
        cy.reportSkill(2, 2, user, moment.utc()
            .subtract(6, 'days')
            .format(dateFormat));
        cy.reportSkill(2, 2, user, moment.utc()
            .subtract(3, 'days')
            .format(dateFormat));

        cy.visit('/progress-and-rankings');
        cy.injectAxe();
        cy.get('[data-cy="viewUsageBtn"]')
            .click();

        cy.contains('Your Daily Usage History');
        cy.contains('6 months');
        cy.get('[data-cy=eventHistoryChartProjectSelector]')
            .contains('This is project 2')
            .should('be.visible');

        cy.wait(1500);
        cy.customLighthouse();
        cy.customA11y();
    });

    it('self report admin page', () => {
        Cypress.Commands.add('rejectRequest', (requestNum = 0, rejectionMsg = 'Skill was rejected') => {
            cy.request('/admin/projects/proj1/approvals?limit=10&ascending=true&page=1&orderBy=userId')
                .then((response) => {
                    cy.request('POST', '/admin/projects/proj1/approvals/reject', {
                        skillApprovalIds: [response.body.data[requestNum].id],
                        rejectionMessage: rejectionMsg,
                    });
                });
        });
        Cypress.Commands.add('approveAllRequests', () => {
            cy.request('/admin/projects/proj1/approvals?limit=10&ascending=true&page=1&orderBy=userId')
                .then((response) => {
                    response.body.data.forEach((item) => {
                        cy.wait(200); // that way sort works properly
                        cy.request('POST', '/admin/projects/proj1/approvals/approve', {
                            skillApprovalIds: [item.id],
                        });
                    });
                });
        });

        cy.createProject(1);
        cy.createSubject(1, 1);
        cy.createSkill(1, 1, 1, { selfReportingType: 'Approval' });
        cy.createSkill(1, 1, 2, { selfReportingType: 'Approval' });
        cy.createSkill(1, 1, 3, { selfReportingType: 'Approval' });
        cy.reportSkill(1, 2, 'user6', '2020-09-11 11:00');
        cy.reportSkill(1, 2, 'user5', '2020-09-12 11:00');
        cy.reportSkill(1, 2, 'user4', '2020-09-13 11:00');
        cy.approveAllRequests();
        cy.reportSkill(1, 2, 'user2', '2020-09-16 11:00');
        cy.rejectRequest(0);

        cy.reportSkill(1, 3, 'user1', '2020-09-17 11:00');
        cy.reportSkill(1, 1, 'user0', '2020-09-18 11:00');
        cy.approveAllRequests();

        cy.reportSkill(1, 2, 'user3', '2020-09-14 11:00');
        cy.rejectRequest(0);

        cy.reportSkill(1, 2, 'user8', '2020-09-14 11:00');
        cy.reportSkill(1, 2, 'user9', '2020-09-14 11:00');

        cy.visit('/administrator/projects/proj1/self-report');
        cy.get('[data-cy="skillsReportApprovalTable"] [data-cy="skillsBTableTotalRows"]').should('have.text', 2)
        cy.get('[data-cy="selfReportApprovalHistoryTable"] [data-cy="skillsBTableTotalRows"]').should('have.text', 7)
        cy.injectAxe();

        cy.customLighthouse();
        cy.customA11y();
    });

    if (!Cypress.env('oauthMode')) {
        it('user tags on the metrics page', () => {
            const userTagsTableSelector = '[data-cy="userTagsTable"]';

            cy.createProject(1);
            cy.createSubject(1, 1);
            cy.createSkill(1, 1, 1);

            cy.logout();
            cy.fixture('vars.json')
                .then((vars) => {
                    cy.login(vars.rootUser, vars.defaultPass);
                });

            Cypress.Commands.add('addUserTag', (userId, tagKey, tags) => {
                cy.request('POST', `/root/users/${userId}/tags/${tagKey}`, { tags });
            });

            const createTags = (numTags, tagKey) => {
                for (let i = 0; i < numTags; i += 1) {
                    const userId = `user${i}`;
                    cy.reportSkill(1, 1, userId, 'now');

                    const tags = [];
                    for (let j = 0; j <= i; j += 1) {
                        tags.push(`tag${j}`);
                    }
                    cy.addUserTag(userId, tagKey, tags);
                }
            };

            createTags(21, 'someValues');
            createTags(25, 'manyValues');

            cy.logout();
            cy.fixture('vars.json')
                .then((vars) => {
                    cy.login(vars.defaultUser, vars.defaultPass);
                });

            cy.intercept('GET', '/public/config', (req) => {
                req.reply({
                    body: {
                        projectMetricsTagCharts: '[{"key":"manyValues","type":"table","title":"Many Values","tagLabel":"Best Label"},{"key":"someValues","type":"bar","title":"Some Values"}]'
                    },
                });
            })
                .as('getConfig');
            cy.viewport(1200, 1000);

            cy.visit('/administrator/projects/proj1/');
            cy.wait('@getConfig');
            cy.injectAxe();

            cy.clickNav('Metrics');
            cy.get('[data-cy="userTagTableCard"] [data-cy="metricsCard-header"]')
                .contains('Many Values');
            cy.get(`${userTagsTableSelector} th`)
                .contains('Best Label');
            cy.get(`${userTagsTableSelector} th`)
                .contains('# Users');

            cy.wait(3000);

            cy.customLighthouse();
            cy.customA11y();
        });

        it('skills groups', () => {
            cy.createProject(1);
            cy.createSubject(1, 1);
            cy.createSkillsGroup(1, 1, 1);
            cy.addSkillToGroup(1, 1, 1, 11, {
                pointIncrement: 10,
                numPerformToCompletion: 5
            });
            cy.addSkillToGroup(1, 1, 1, 22, {
                pointIncrement: 10,
                numPerformToCompletion: 5
            });
            const groupId = 'group1';

            cy.visit('/administrator/projects/proj1/subjects/subj1');
            cy.injectAxe();

            cy.get(`[data-cy="expandDetailsBtn_${groupId}"]`)
                .click();
            cy.get('[data-cy="editSkillButton_skill11"]');
            cy.get('[data-cy="editSkillButton_skill22"]');

            cy.customLighthouse();
            cy.customA11y();

            cy.createSkillsGroup(1, 1, 1, {
                numSkillsRequired: 1,
                enabled: true
            });

            cy.visit('/administrator/projects/proj1/subjects/subj1');
            cy.injectAxe();

            cy.get(`[data-cy="expandDetailsBtn_${groupId}"]`)
                .click();
            cy.get('[data-cy="editSkillButton_skill11"]');
            cy.get('[data-cy="editSkillButton_skill22"]');

            cy.customLighthouse();
            cy.customA11y();

            cy.createSkillsGroup(1, 1, 1, {
                numSkillsRequired: -1,
                enabled: true
            });
            cy.addSkillToGroup(1, 1, 1, 33, {
                pointIncrement: 11,
                numPerformToCompletion: 2
            });
            cy.visit('/administrator/projects/proj1/subjects/subj1');
            cy.injectAxe();
        });
    }

    it('catalog page', () => {
        cy.createProject(1);
        cy.createSubject(1, 1);

        cy.createSkill(1, 1, 1);
        cy.createSkill(1, 1, 2);
        cy.createSkill(1, 1, 3);

        cy.exportSkillToCatalog(1, 1, 1);
        cy.exportSkillToCatalog(1, 1, 2);
        cy.exportSkillToCatalog(1, 1, 3);

        cy.visit('/administrator/projects/proj1/skills-catalog');
        cy.injectAxe();
        cy.validateTable('[data-cy="exportedSkillsTable"]', [
            [{
                colIndex: 0,
                value: 'Very Great Skill 3'
            }],
            [{
                colIndex: 0,
                value: 'Very Great Skill 2'
            }],
            [{
                colIndex: 0,
                value: 'Very Great Skill 1'
            }],
        ], 5);
        cy.customLighthouse();
        cy.customA11y();

        // Delete from Catalog modal
        cy.get('[data-cy="deleteSkillButton_skill2"]')
            .click();
        cy.contains('This will PERMANENTLY remove [Very Great Skill 2] Skill');
        cy.customLighthouse();
        cy.customA11y();
    });

    it('import from catalog modal', () => {
        cy.createProject(1);
        cy.createSubject(1, 1);

        cy.createSkill(1, 1, 1, { description: '# This is where description goes\n\ntest test' });
        cy.createSkill(1, 1, 2);
        cy.createSkill(1, 1, 3);

        cy.exportSkillToCatalog(1, 1, 1);
        cy.exportSkillToCatalog(1, 1, 2);
        cy.exportSkillToCatalog(1, 1, 3);

        cy.createProject(2);
        cy.createSubject(2, 1);

        cy.visit('/administrator/projects/proj2/subjects/subj1');
        cy.injectAxe();
        cy.get('[data-cy="importFromCatalogBtn"]')
            .click();
        cy.get('[data-cy="expandDetailsBtn_proj1_skill1"]')
            .click();
        cy.contains('This is where description goes');

        cy.customLighthouse();
        cy.customA11y();
    });

    it('export to catalog modal', () => {
        cy.createProject(1);
        cy.createSubject(1, 1);

        cy.createSkill(1, 1, 1);
        cy.createSkill(1, 1, 2);
        cy.createSkill(1, 1, 3);

        cy.exportSkillToCatalog(1, 1, 1);
        cy.exportSkillToCatalog(1, 1, 2);
        cy.exportSkillToCatalog(1, 1, 3);

        cy.createProject(2);
        cy.createSubject(2, 1);
        cy.createSkill(2, 1, 1);
        cy.createSkill(2, 1, 2, { name: 'Something Else' });
        cy.createSkill(2, 1, 3, { skillId: 'diffId' });
        cy.createSkill(2, 1, 4);
        cy.createSkill(2, 1, 5);
        cy.createSkill(2, 1, 6);
        cy.assignDep(2, 5, 6);
        cy.createSkill(2, 1, 7);
        cy.exportSkillToCatalog(2, 1, 7);

        cy.visit('/administrator/projects/proj2/subjects/subj1');
        cy.injectAxe();
        cy.get('[data-cy="selectAllSkillsBtn"]')
            .click();

        cy.get('[data-cy="skillActionsBtn"] button')
            .click();
        cy.get('[data-cy="skillExportToCatalogBtn"]')
            .click();
        cy.contains('Note: The are already 1 skill(s) in the Skill Catalog from the provided selection.');
        cy.contains('This will export 2 Skills');
        cy.get('[data-cy="dupSkill-skill1"]')
            .contains('ID Conflict');
        cy.get('[data-cy="dupSkill-skill1"]')
            .contains('Name Conflict');
        cy.get('[data-cy="dupSkill-skill2"]')
            .contains('ID Conflict');
        cy.get('[data-cy="dupSkill-diffId"]')
            .contains('Name Conflict');
        cy.get('[data-cy="dupSkill-skill5"]')
            .contains('Has Dependencies');

        cy.customLighthouse();
        cy.customA11y();
    });

    it('configure self approval workload', function () {
        const pass = 'password';
        cy.register('user1', pass);
        cy.register('user2', pass);
        cy.register('user3', pass);
        cy.fixture('vars.json')
            .then((vars) => {
                if (!Cypress.env('oauthMode')) {
                    cy.log('NOT in oauthMode, using form login');
                    cy.login(vars.defaultUser, vars.defaultPass);
                } else {
                    cy.log('oauthMode, using loginBySingleSignOn');
                    cy.loginBySingleSignOn();
                }
            });
        cy.request('POST', `/admin/projects/MyNewtestProject/users/user1/roles/ROLE_PROJECT_APPROVER`);
        cy.request('POST', `/admin/projects/MyNewtestProject/users/user2/roles/ROLE_PROJECT_APPROVER`);
        cy.request('POST', `/admin/projects/MyNewtestProject/approverConf/user1`, { userId: 'u1' });
        cy.request('POST', `/admin/projects/MyNewtestProject/approverConf/user1`, { userId: 'u2' });
        cy.request('POST', `/admin/projects/MyNewtestProject/approverConf/user1`, { userId: 'u3' });
        cy.request('POST', `/admin/projects/MyNewtestProject/approverConf/user1`, { userId: 'u4' });
        cy.request('POST', `/admin/projects/MyNewtestProject/approverConf/user1`, { userId: 'u5' });
        cy.request('POST', `/admin/projects/MyNewtestProject/approverConf/user1`, {
            userTagKey: 'tagKey',
            userTagValue: 'tagValue'
        });
        cy.request('POST', `/admin/projects/MyNewtestProject/approverConf/user1`, {
            skillId: 'skill1'
        });
        cy.request('POST', `/admin/projects/MyNewtestProject/approverConf/user1`, {
            skillId: 'skill2'
        });
        cy.visit('/administrator/projects/MyNewtestProject/self-report/configure');
        cy.injectAxe();

        cy.get(`[data-cy="workloadCell_user1"] [data-cy="editApprovalBtn"]`).click()
        const tableSelector = `[data-cy="expandedChild_user1"] [data-cy="skillApprovalConfSpecificUsersTable"]`
        cy.get(`${tableSelector} [data-cy="skillsBTableTotalRows"]`).should('have.text', '5')

        cy.get(`[data-cy="workloadCell_user2"] [data-cy="editApprovalBtn"]`).click()
        cy.get(`[data-cy="expandedChild_user2"] [data-cy="noUserConf"]`).should('exist')

        cy.customLighthouse();
        cy.customA11y();
    });
});
