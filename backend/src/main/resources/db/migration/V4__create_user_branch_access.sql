CREATE TABLE user_branch_access (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    branch_id UUID NOT NULL REFERENCES branch(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, branch_id)
);

ALTER TABLE app_user ADD COLUMN full_name VARCHAR(180);

UPDATE app_user
SET full_name = split_part(email, '@', 1)
WHERE full_name IS NULL;

ALTER TABLE app_user ALTER COLUMN full_name SET NOT NULL;

INSERT INTO user_branch_access (user_id, branch_id)
SELECT id, active_branch_id
FROM app_user
WHERE active_branch_id IS NOT NULL
ON CONFLICT DO NOTHING;

CREATE INDEX user_branch_access_branch_idx ON user_branch_access(branch_id);
