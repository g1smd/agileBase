package com.gtwm.pb.util;

import org.jwalk.GeneratorException;
import org.jwalk.gen.CustomGenerator;
import org.jwalk.gen.MasterGenerator;
import org.jwalk.gen.StringGenerator;
import com.gtwm.pb.auth.Company;
import com.gtwm.pb.model.interfaces.CompanyInfo;

public class AbGenerator implements CustomGenerator {

	@Override
	public boolean canCreate(Class<?> testClass) {
		if (testClass.equals(CompanyInfo.class)) {
			return true;
		}
		return false;
	}

	@Override
	public Object nextValue(Class<?> testClass) throws GeneratorException {
		if (testClass.equals(CompanyInfo.class)) {
			return new Company((String) new StringGenerator().nextValue(String.class));
		}
		throw new GeneratorException(testClass);
	}

	@Override
	public void setOwner(MasterGenerator testClass) {
		// TODO Auto-generated method stub

	}

}
