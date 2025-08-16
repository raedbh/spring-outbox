/*
 *  Copyright 2024-2025 the original authors.
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

import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

import email.contracts.EmailMessageBody;
import email.contracts.EmailMessageBody.Contact;
import sample.sourcing.proposal.Proposal.VendorContact;

/**
 * @author Raed Ben Hamouda
 */
@Mapper(componentModel = "spring")
public interface EmailNotificationConverter extends Converter<EmailNotification, EmailMessageBody> {

    @Override
    @Mapping(target = "to", expression = "java(createContactList(source.to()))")
    @Mapping(target = "locale", expression = "java(java.util.Locale.ENGLISH)")
    @Mapping(target = "cc", ignore = true)
    @Mapping(target = "bcc", ignore = true)
    @Mapping(target = "lookAndFeel", ignore = true)
    EmailMessageBody convert(EmailNotification source);

    default List<Contact> createContactList(VendorContact vendorContact) {
        return Collections.singletonList(new Contact(vendorContact.email(), vendorContact.name()));
    }
}
