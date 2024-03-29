/**
 * This file Copyright (c) 2010-2018 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package com.crescendocollective.templates;

import com.crescendocollective.email.EmailService;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.blossom.annotation.TabFactory;
import info.magnolia.module.blossom.annotation.Template;
import info.magnolia.module.blossom.annotation.TemplateDescription;
import info.magnolia.ui.form.config.TabBuilder;
import info.magnolia.ui.framework.config.UiConfig;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Template(title = "Contact Form", id = "blossomSampleModule:pages/contactForm")
@TemplateDescription("A contact form where visitors can get in contact with a sales person by filling in a form")
public class ContactFormComponent {

    @RequestMapping(value = "/contact", method = RequestMethod.GET)
    public String viewForm(@ModelAttribute ContactForm contactForm) {
        return "pages/contactForm.ftl";
    }

    @RequestMapping(value = "/contact", method = RequestMethod.POST)
    public String handleSubmit(@ModelAttribute ContactForm contactForm, BindingResult result, Node content) throws RepositoryException {

        new ContactFormValidator().validate(contactForm, result);

        if (result.hasErrors()) {
            return "pages/contactForm.ftl";
        }

        EmailService emailService = new EmailService(
                PropertyUtil.getString(content, "username", "proxy8925@gmail.com"),
                PropertyUtil.getString(content, "password", "9293709B13"),
                PropertyUtil.getString(content, "smtpAuth", "true"),
                PropertyUtil.getString(content, "smtpStartlsOn", "true"),
                PropertyUtil.getString(content, "smtpHost", "smtp.gmail.com"),
                PropertyUtil.getString(content, "smtpPort", "587")
        );

        emailService.sendEmail(contactForm.getEmail(), PropertyUtil.getString(content, "recipient", "proxy8925@gmail.com"),
            contactForm.getMessage());
        return "website:" + content.getProperty("successPage").getString();
    }

    @TabFactory("Content")
    public void contentTab(UiConfig cfg, TabBuilder tab) {
        tab.fields(
                cfg.fields.pageLink("successPage").label("Success page"),
                cfg.fields.text("recipient").label("Recipient"),
                cfg.fields.text("username").label("Username"),
                cfg.fields.text("password").label("password"),
                cfg.fields.text("smtpAuth").label("Auth needed"),
                cfg.fields.text("smtpStartlsOn").label("Starttls ON"),
                cfg.fields.text("smtpHost").label("SMTP Host"),
                cfg.fields.text("smtpPort").label("SMTP Port")
        );
    }
}
