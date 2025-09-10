package com.longcovidspa.backend.repositories;

import com.longcovidspa.backend.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface ProviderDirectoryRepo extends JpaRepository<ProviderDirectory, Long> {
    Optional<ProviderDirectory> findByLicenseNumberAndLicenseIssuer(String licenseNumber, String licenseIssuer);
}
