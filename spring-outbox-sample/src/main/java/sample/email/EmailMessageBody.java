/*
 *  Copyright 2024 the original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.email;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * The event-contract class for sending an Email.
 *
 * @author Raed Ben Hamouda
 */
public class EmailMessageBody implements Serializable {

    private String type;
    private Locale locale;
    private List<Contact> to;
    private List<Contact> cc;
    private List<Contact> bcc;
    private LookAndFeel lookAndFeel;
    private Map<String, Serializable> templateParams;


    public EmailMessageBody(String type, Locale locale, List<Contact> to, List<Contact> cc, List<Contact> bcc,
      LookAndFeel lookAndFeel, Map<String, Serializable> templateParams) {

        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("to cannot be empty");
        }
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.locale = Objects.requireNonNull(locale, "locale cannot be null");
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.lookAndFeel = lookAndFeel;
        this.templateParams = templateParams;
    }


    public String getType() {
        return type;
    }

    public Locale getLocale() {
        return locale;
    }

    public List<Contact> getTo() {
        return to;
    }

    public List<Contact> getCc() {
        return cc;
    }

    public List<Contact> getBcc() {
        return bcc;
    }

    public LookAndFeel getLookAndFeel() {
        return lookAndFeel;
    }

    public Map<String, Serializable> getTemplateParams() {
        return templateParams;
    }


    public static class Contact implements Serializable {

        private String email;
        private String name;

        public Contact(String email) {
            this.email = email;
        }

        public Contact(String email, String name) {
            this.email = Objects.requireNonNull(email, "email cannot be null");
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }
    }


    public static class LookAndFeel implements Serializable {

        private String logo;
        private String color;

        public LookAndFeel(String logo, String color) {
            this.logo = logo;
            this.color = color;
        }

        public String getLogo() {
            return logo;
        }

        public String getColor() {
            return color;
        }
    }

}
