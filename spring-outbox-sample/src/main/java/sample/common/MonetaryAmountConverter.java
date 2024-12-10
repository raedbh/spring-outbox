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

package sample.common;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.javamoney.moneta.Money;

/**
 * @author Raed Ben Hamouda
 */
@Converter(autoApply = true)
public class MonetaryAmountConverter implements AttributeConverter<MonetaryAmount, BigDecimal> {

		@Override
		public BigDecimal convertToDatabaseColumn(MonetaryAmount amount) {
				return amount == null ? null : amount.getNumber().numberValue(BigDecimal.class);
		}

		@Override
		public MonetaryAmount convertToEntityAttribute(BigDecimal column) {
				return column == null ? null : Money.of(column, Currencies.EURO);
		}
}
