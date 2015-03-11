package com.goldfish.registration;

import java.util.EnumMap;

import com.goldfish.services.model.RSAQuestion;


public class RegistrationDataManager {
	
	private EnumMap<RegistrationKey, String> simpleRegistrationData;
	private EnumMap<QuestionKey, RSAQuestion> questionDataMap;

	public static enum RegistrationKey {
		USER_NAME, PASSWORD, SECURITY_IMAGE_PATH, USER_EMAIL, SECURITY_PHRASE, 
		ENROLL_IN_PAPERLESS, CUSTOMER_ID, ACCOUNT_NUMBER, ACCOUNT_LOCKED_FLAG,
		DOB, SSN, CVV, COUNTRY_CITIZENSHIP, COUNTRYNAME, EMAIL, PARTNERNAME,PARTNERPHONENUM, ACCNUM, ISPAPERLESSELIGIBLE, ISMMNPresent,
		ISUSERENROLLEDINPP, ANSWER_1, ANSWER_2, ANSWER_3, ANSWER_4, ANSWER_5, MMN, SECURITY_IMAGE_CATEGORY
	}

	public static enum QuestionKey {
		QUESTION_1, QUESTION_2, QUESTION_3, QUESTION_4, QUESTION_5
	}
	
	public RegistrationDataManager(){
		super();
		simpleRegistrationData = new EnumMap<RegistrationKey, String>(RegistrationKey.class);
		questionDataMap = new EnumMap<QuestionKey, RSAQuestion>(QuestionKey.class);
	}
	
	public void setInRegistrationCache(RegistrationKey key, String value) {
		this.simpleRegistrationData.put(key, value);
	}

	public String getFromRegistrationCache(RegistrationKey key) {
		return this.simpleRegistrationData.get(key);
	}
	
	public void setInQuestionsCache(QuestionKey key, RSAQuestion question) {
		this.questionDataMap.put(key, question);
	}
	
	public RSAQuestion getFromQuestionsCache(QuestionKey key) {
		return this.questionDataMap.get(key);
	}


}