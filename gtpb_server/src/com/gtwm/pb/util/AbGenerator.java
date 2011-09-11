package com.gtwm.pb.util;

import org.jwalk.GeneratorException;
import org.jwalk.gen.CustomGenerator;
import org.jwalk.gen.MasterGenerator;
import com.gtwm.pb.auth.Company;
import com.gtwm.pb.model.interfaces.CompanyInfo;

public class AbGenerator implements CustomGenerator {

	private MasterGenerator owner;

	@Override
	public boolean canCreate(Class<?> type) {
		return type == CompanyInfo.class;
	}

	@Override
	public Object nextValue(Class<?> type) throws GeneratorException {
		if (type == CompanyInfo.class)
			return owner.nextValue(Company.class);
		else
			throw new GeneratorException(type);
	}

	@Override
	public void setOwner(MasterGenerator generator) {
		owner = generator;
	}

}
