INSERT INTO users (first_name, last_name, email, password, role) VALUES
                                                                     ('System', 'Admin', 'admin@medic.com', '$2a$10$y3y4exo.iL0MPCCguU5UWuH62laCBEl4SeFkdYMCNhpkoC8oXF5im', 'ADMIN'),
                                                                     ('John', 'Doe', 'doctor@medic.com', '$2a$10$y3y4exo.iL0MPCCguU5UWuH62laCBEl4SeFkdYMCNhpkoC8oXF5im', 'DOCTOR'),
                                                                     ('Jane', 'Smith', 'patient@medic.com', '$2a$10$y3y4exo.iL0MPCCguU5UWuH62laCBEl4SeFkdYMCNhpkoC8oXF5im', 'PATIENT');


INSERT INTO doctors (specialty, user_id) VALUES
    ('Scoliosis', 2);


INSERT INTO patients (cnp, weight, age, gender, height, user_id) VALUES
    ('2900101123456', 65.5, 34, 'FEMALE', 170.0, 3);