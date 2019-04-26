/*Copyright (c) 2016 "hbz"

This file is part of zettel.

etikett is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package models;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.typesafe.config.ConfigFactory;

import play.data.validation.ValidationError;
import services.ZettelFields;
import services.ZettelHelper;

/**
 * @author Jan Schnasse
 *
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Article extends ZettelModel {

	/**
	 * The id under which this model is registered in the ZettelRegister
	 */
	public final static String id = "katalog:article";

	/**
	 * Thanks Simon G.! http://stackoverflow.com/a/5492927/4420271
	 */
	public final static Pattern URN_PATTERN = Pattern.compile(
			"^urn:[a-z0-9][a-z0-9-]{0,31}:([a-z0-9()+,\\-.:=@;$_!*']|%[0-9a-f]{2})+$",
			Pattern.CASE_INSENSITIVE);

	/**
	 * The help text url must provide help texts in a certain html form. For each
	 * field a <div id="fieldName"> must be provided to contain the help text for
	 * the corresponding field.
	 * 
	 * @return return a url with help texts for the form
	 */
	public static String getHelpTextUrl() {
		String url = ConfigFactory.load().getString("zettel.article.helpText");
		return url;
	}

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();
		validateMandatoryFields(errors);
		validateURLs(errors);
		return errors.isEmpty() ? null : errors;
	}

	private void validateMandatoryFields(List<ValidationError> errors) {
		// Publikationstyp
		mandatoryField(getLabel("rdftype"), getRdftype(), errors);
		// Publikationsstatus
		mandatoryField(getLabel("publicationStatus"), getPublicationStatus(),
				errors);
		// Titel
		mandatoryField(getLabel("title"), getTitle(), errors);
		// Autoren
		validateAuthorship(errors);
		// Erschienen in
		mandatoryField(getLabel("containedIn"), getContainedIn(), errors);
		// Online veröffentlicht
		mandatoryField(getLabel("publicationYear"), getPublicationYear(), errors);
		// Sprache der Publikation
		mandatoryField(getLabel("language"), getLanguage(), errors);
		// Fächerklassifikation
		mandatoryField(getLabel("ddc"), getDdc(), errors);
	}

	private void validateURLs(List<ValidationError> errors) {
		validate(getLabel("publisherVersion"), getPublisherVersion(), errors);
		validate(getLabel("fulltextVersion"), getFulltextVersion(), errors);
		validate(getLabel("additionalMaterial"), getAdditionalMaterial(), errors);
		validate(getLabel("internalReference"), getAdditionalMaterial(), errors);
		validate(getLabel("license"), Arrays.asList(getLicense()), errors);
		validate(getLabel("creator"), getCreator(), errors);
		validate(getLabel("contributor"), getContributor(), errors);
		validate(getLabel("editor"), getEditor(), errors);
		validate(getLabel("Other"), getOther(), errors);
		validate(getLabel("containedIn"), getContainedIn(), errors);
		validate(getLabel("institution"), getInstitution(), errors);
		validate(getLabel("collectionOne"), getCollectionOne(), errors);
		validate(getLabel("collectionTwo"), getCollectionTwo(), errors);
		validate(getLabel("ddc"), getDdc(), errors);
		validate(getLabel("additionalMaterial"), getAdditionalMaterial(), errors);
		validate(getLabel("internalReference"), getInternalReference(), errors);
		validate(getLabel("fundingId"), getFundingId(), errors);
		validate(getLabel("publisherVersion"), getPublisherVersion(), errors);
		validate(getLabel("fulltextVersion"), getFulltextVersion(), errors);
	}

	private void validate(String fieldLabel, List<String> fieldContent,
			List<ValidationError> errors) {
		play.Logger.debug("Validiere " + fieldLabel);
		if (fieldContent == null || fieldContent.isEmpty())
			return;
		fieldContent.forEach(v -> {
			if (v != null && !v.isEmpty() && !isValid(v)) {
				errors.add(new ValidationError(fieldLabel,
						String.format(
								"Die Eingabe \"" + v + "\" hat nicht die Form einer URL.",
								fieldLabel)));
			}
		});

	}

	@SuppressWarnings({ "javadoc", "unused" })
	public boolean isValid(String addr) {
		try {
			new URL(addr);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void validateAuthorship(List<ValidationError> errors) {
		if (containsNothing(getCreator())) {
			setCreator(new ArrayList<>());
		}
		if (containsNothing(getContributor())) {
			setContributor(new ArrayList<>());
		}

		if (getCreator().isEmpty() && getContributor().isEmpty()) {
			errors.add(new ValidationError(getLabel("creator"),
					"Bitte machen sie in einem der folgenden Felder mindestens eine Angabe: \"Autor/in\", \"Mitwirkende/r\"!"));
			errors.add(new ValidationError(getLabel("contributor"),
					"Bitte machen sie in einem der folgenden Felder mindestens eine Angabe: \"Autor/in\", \"Mitwirkende/r\"!"));
		}
	}

	@SuppressWarnings("static-method")
	private void mandatoryField(String fieldLabel, List<String> fieldContent,
			List<ValidationError> errors) {

		if (containsNothing(fieldContent)) {
			errors.add(new ValidationError(fieldLabel,
					String.format(
							"Es ist mindestens ein Eintrag im Feld \"%s\" erforderlich!",
							fieldLabel)));
		}
	}

	@SuppressWarnings("static-method")
	private void mandatoryField(String fieldLabel, String fieldContent,
			List<ValidationError> errors) {

		if (fieldContent == null || fieldContent.isEmpty()) {
			errors.add(new ValidationError(fieldLabel,
					String.format("Bitte füllen Sie das Feld \"%s\" aus!", fieldLabel)));
		}
	}

}