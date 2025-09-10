-- Directory of allowed clinicians (optional: preload known medics)
CREATE TABLE IF NOT EXISTS provider_directory (
  id BIGSERIAL PRIMARY KEY,
  full_name TEXT NOT NULL,
  license_number TEXT NOT NULL UNIQUE,
  license_issuer TEXT NOT NULL,
  work_email_domain TEXT,
  active BOOLEAN DEFAULT TRUE
);

-- Applications submitted by users
CREATE TABLE IF NOT EXISTS medic_application (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  provider_directory_id BIGINT,
  license_number TEXT NOT NULL,
  license_issuer TEXT NOT NULL,
  work_email TEXT NOT NULL,
  status TEXT NOT NULL, -- PENDING / APPROVED / REJECTED
  submitted_at TIMESTAMP NOT NULL DEFAULT NOW(),
  reviewed_by BIGINT,
  reviewed_at TIMESTAMP,
  reject_reason TEXT,
  doc_urls JSONB NOT NULL DEFAULT '[]'::jsonb,
  email_verified BOOLEAN DEFAULT FALSE,
  email_token TEXT,
  CONSTRAINT fk_medapp_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_medapp_dir  FOREIGN KEY (provider_directory_id) REFERENCES provider_directory (id)
);

-- Medic <-> Patient access
CREATE TABLE IF NOT EXISTS patient_assignment (
  id BIGSERIAL PRIMARY KEY,
  medic_id BIGINT NOT NULL,
  patient_id BIGINT NOT NULL,
  granted_at TIMESTAMP NOT NULL DEFAULT NOW(),
  revoked_at TIMESTAMP,
  CONSTRAINT fk_assign_medic   FOREIGN KEY (medic_id)   REFERENCES users (id),
  CONSTRAINT fk_assign_patient FOREIGN KEY (patient_id) REFERENCES users (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_patient_assignment_active
ON patient_assignment (medic_id, patient_id)
WHERE revoked_at IS NULL;
