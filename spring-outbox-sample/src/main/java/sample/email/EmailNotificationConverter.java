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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.core.convert.converter.Converter;

import sample.email.EmailMessageBody.Contact;

/**
 * @author Raed Ben Hamouda
 */
@Mapper(componentModel = "spring")
public interface EmailNotificationConverter extends Converter<Map<String, Object>, EmailMessageBody> {

		@Mapping(target = "type", source = "source.type", qualifiedByName = "convertToString")
		@Mapping(target = "to", expression = "java(createContactList(source.get(\"toEmail\")))")
		@Mapping(target = "cc", ignore = true)
		@Mapping(target = "bcc", ignore = true)
		@Mapping(target = "lookAndFeel", ignore = true)
		@Mapping(target = "templateParams", ignore = true)
		EmailMessageBody map(Map<String, Object> source);

		default EmailMessageBody convert(Map<String, Object> source) {
				return map(source);
		}

		default Contact createContact(Object email) {
				return new Contact(email.toString());
		}

		default List<Contact> createContactList(Object email) {
				return Collections.singletonList(createContact(email));
		}

		@Named("convertToString")
		default String convertToString(Object value) {
				return (value != null) ? value.toString() : null;
		}

		default Locale map(Object value) {
				return (Locale) value;
		}

}
