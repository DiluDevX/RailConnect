-- Keeps V2 immutable while correcting the documented development-only password.
UPDATE users
SET password_hash = '$2a$10$1AgBVwWQgV3jrx4G2/.7j.K0luewoslGnhagCdm8gpviJ6aSt0mla',
    updated_at = NOW()
WHERE email IN (
    'admin@railconnect.lk',
    'officer@railconnect.lk',
    'passenger@railconnect.lk'
);
