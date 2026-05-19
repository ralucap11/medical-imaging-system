INSERT INTO users (first_name, last_name, email, password, role) VALUES
                                                                     ('System', 'Admin', 'admin@medic.com', '$2a$10$y3y4exo.iL0MPCCguU5UWuH62laCBEl4SeFkdYMCNhpkoC8oXF5im', 'ADMIN'),
                                                                     ('John', 'Doe', 'doctor@medic.com', '$2a$10$y3y4exo.iL0MPCCguU5UWuH62laCBEl4SeFkdYMCNhpkoC8oXF5im', 'DOCTOR'),
                                                                     ('Jane', 'Smith', 'patient@medic.com', '$2a$10$y3y4exo.iL0MPCCguU5UWuH62laCBEl4SeFkdYMCNhpkoC8oXF5im', 'PATIENT');

-- 2. Insert Doctor Entity
-- Link to user_id = 2 (John Doe)
INSERT INTO doctors (specialty, user_id) VALUES
    ('Scoliosis', 2);

-- 3. Insert Patient Entity
-- Link to user_id = 3 (Jane Smith). Assuming Gender enum string values are MALE/FEMALE
INSERT INTO patients (cnp, weight, age, gender, height, user_id) VALUES
    ('2900101123456', 65.5, 34, 'FEMALE', 170.0, 3);