package org.zlwima.emurgency.registration.shared;

public class FieldVerifier {

	private String escapeHtml( String html ) {
		if( html == null ) {
			return null;
		}
		return html.replaceAll( "&", "&amp;" ).replaceAll( "<", "&lt;" ).replaceAll(
				">", "&gt;" );
	}

	public static boolean isValidInteger( String text, int min, int max ) {
		try {
			int nr = Integer.parseInt( text );
			if( nr < min || nr > max ) {
				throw( new NumberFormatException() );
			}
			return true;
		} catch( NumberFormatException nfe ) {
			return false;
		}
	}
	
	public static boolean isValidName( String name ) {
		if( name == null ) {
			return false;
		}
		return name.length() > 3;
	}

	public static boolean isValidEmail( String email ) {
		if( email == null ) {
			return false;
		}
		Boolean valid;
		String emailPattern = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$";
		valid = email.matches( emailPattern );
		return valid;
	}

	public static boolean isValidMobileNumber( String mNumber ) {
		if( mNumber == null ) {
			return false;
		}
		Boolean valid;
		String MNumberPattern = "^(+)*[0-9]+$";
		valid = mNumber.matches( MNumberPattern );
		return valid;
	}

	public static boolean isValidDate( String date ) {
		if( date == null ) {
			return false;
		}
		Boolean valid;
		String DatePattern = "";
		valid = date.matches( DatePattern );
		return valid;
	}

	public static boolean isValidGender( String gender ) {
		if( gender == null ) {
			return false;
		}
		Boolean valid;
		String GenderPattern = "^[male|female]";
		valid = gender.matches( GenderPattern );
		return valid;
	}
}
