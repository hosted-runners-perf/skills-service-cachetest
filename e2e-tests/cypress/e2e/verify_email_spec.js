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
describe('Verify Email Tests', () => {

  if (Cypress.env('verifyEmail') === true || Cypress.env('verifyEmail') === 'true') {
    beforeEach(() => {
      cy.logout();
      cy.resetEmail();

      cy.fixture('vars.json').then((vars) => {
        cy.register(vars.rootUser, vars.defaultPass, true);
      });

      cy.login('root@skills.org', 'password');

      cy.request({
        method: 'POST',
        url: '/root/saveEmailSettings',
        body: {
          host: 'localhost',
          port: 1026,
          'protocol': 'smtp'
        },
      });

      cy.request({
        method: 'POST',
        url: '/root/saveSystemSettings',
        body: {
          publicUrl: 'http://localhost:8082/',
          resetTokenExpiration: 'PT2H',
          fromEmail: 'noreploy@skilltreeemail.org',
        }
      });

      cy.logout();

      cy.intercept({
        method: 'POST',
        url: '/verifyEmail'
      }).as('verifyEmail');

      cy.intercept('GET', '/app/projects/**').as('getProjects')
      cy.intercept('GET', '/app/userInfo/**').as('getUserInfo')
    });

    it('register dashboard and confirm email address', () => {
      cy.visit('/request-account');
      cy.contains('New Account')
      cy.get('#firstName').type("Robert")
      cy.get('#lastName').type("Smith")
      cy.get('#email').type("rob.smith@madeup.org")
      cy.get('#password').type("password")
      cy.get('#password_confirmation').type("password")
      cy.contains('Create Account').click()

      cy.get('[data-cy="emailVerificationSentConfirmation"]').should('be.visible')
        .and('contain', 'Email Verification Sent!');

      cy.getLinkFromEmail().then((confirmEmailLink) => {
        cy.visit(confirmEmailLink);
        cy.wait('@verifyEmail');
        cy.get('[data-cy="emailConfirmation"]').should('be.visible')
          .and('contain', 'Email Address Successfully Confirmed!');
        cy.wait(11*1000) //will redirect to login page after 10 seconds
        cy.get('[data-cy=login]').should('exist');
        cy.get('#username').type('rob.smith@madeup.org');
        cy.get('#inputPassword').type('password');
        cy.get('[data-cy=login]').click();
        cy.wait('@getProjects');
        cy.wait('@getUserInfo');

        cy.get('[data-cy="breadcrumb-Progress And Rankings"]').should('be.visible');
      });
    });

    it('cannot use email confirmation link twice', () => {
      cy.visit('/request-account');
      cy.contains('New Account')
      cy.get('#firstName').type("Robert")
      cy.get('#lastName').type("Smith")
      cy.get('#email').type("rob.smith@madeup.org")
      cy.get('#password').type("password")
      cy.get('#password_confirmation').type("password")
      cy.contains('Create Account').click()

      cy.get('[data-cy="emailVerificationSentConfirmation"]').should('be.visible')
        .and('contain', 'Email Verification Sent!');

      cy.getLinkFromEmail().then((confirmEmailLink) => {
        cy.visit(confirmEmailLink);
        cy.wait('@verifyEmail');
        cy.get('[data-cy="emailConfirmation"]').should('be.visible')
          .and('contain', 'Email Address Successfully Confirmed!');
        cy.visit(confirmEmailLink);
        cy.wait('@verifyEmail').its('response.body').should(
          'have.property',
          'explanation',
          'The supplied email verification token does not exist or is not for the specified user.')
        cy.get('[data-cy=confirmEmailTitle]').contains('Email Verification is Required!');
        cy.get('[data-cy=confirmEmailExplanation]').contains('An error occurred while verifying your email address. Please click the button below to resend a new verification code.');
      });
    });

    it('user is redirected to confirm email address if login attempted before confirming', () => {
      cy.visit('/request-account');
      cy.contains('New Account')
      cy.get('#firstName').type("Robert")
      cy.get('#lastName').type("Smith")
      cy.get('#email').type("rob.smith@madeup.org")
      cy.get('#password').type("password")
      cy.get('#password_confirmation').type("password")
      cy.contains('Create Account').click()

      cy.get('[data-cy="emailVerificationSentConfirmation"]').should('be.visible')
        .and('contain', 'Email Verification Sent!');

      cy.visit('/');
      cy.get('[data-cy=login]').should('exist');
      cy.get('#username').type('rob.smith@madeup.org');
      cy.get('#inputPassword').type('password');
      cy.get('[data-cy=login]').click();

      cy.get('[data-cy=confirmEmailTitle]').contains('Email Verification is Required!');
      cy.get('[data-cy=confirmEmailExplanation]').contains('You must first validate your email address in order to start using SkillTree.');
      cy.get('[data-cy=resendConfirmationCodeButton]').should('be.visible')
    });

    it('user is redirected to confirm email address if token has expired', () => {
      // override timeout setting to 5 seconds
      cy.login('root@skills.org', 'password');
      cy.request({
        method: 'POST',
        url: '/root/saveSystemSettings',
        body: {
          resetTokenExpiration: 'PT5S',
          publicUrl: 'http://localhost:8082/',
          fromEmail: 'noreploy@skilltreeemail.org',
        }
      });
      cy.logout();

      cy.visit('/request-account');
      cy.contains('New Account')
      cy.get('#firstName').type("Robert")
      cy.get('#lastName').type("Smith")
      cy.get('#email').type("rob.smith@madeup.org")
      cy.get('#password').type("password")
      cy.get('#password_confirmation').type("password")
      cy.contains('Create Account').click()

      cy.get('[data-cy="emailVerificationSentConfirmation"]').should('be.visible')
        .and('contain', 'Email Verification Sent!');

      cy.wait(6*1000)
      cy.getLinkFromEmail().then((confirmEmailLink) => {
        cy.visit(confirmEmailLink);

        // link is expired, request a new one
        cy.get('[data-cy=confirmEmailTitle]').contains('Email Verification is Required!');
        cy.get('[data-cy=confirmEmailExplanation]').contains('Your email verification code has expired. Please click the button below to resend a new verification code.');
        cy.get('[data-cy=resendConfirmationCodeButton]').click()

        cy.get('[data-cy="emailVerificationSentConfirmation"]').should('be.visible')
          .and('contain', 'Email Verification Sent!');

        // confirm email with new link and login
        cy.getLinkFromEmail().then((confirmEmailLink) => {
          cy.visit(confirmEmailLink);
          cy.wait('@verifyEmail');
          cy.get('[data-cy="emailConfirmation"]').should('be.visible')
            .and('contain', 'Email Address Successfully Confirmed!');
          cy.wait(11*1000) //will redirect to login page after 10 seconds
          cy.get('[data-cy=login]').should('exist');
          cy.get('#username').type('rob.smith@madeup.org');
          cy.get('#inputPassword').type('password');
          cy.get('[data-cy=login]').click();
          cy.wait('@getProjects');
          cy.wait('@getUserInfo');

          cy.get('[data-cy="breadcrumb-Progress And Rankings"]').should('be.visible');
        });
      });
    });
  }
});
