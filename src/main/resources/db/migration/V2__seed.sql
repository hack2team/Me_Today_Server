-- Seed interests
INSERT INTO interests (name, description, icon_url) VALUES
('자기개발', '개인 성장과 발전에 관심', 'https://example.com/icons/self-dev.png'),
('여행', '새로운 장소 탐험과 경험', 'https://example.com/icons/travel.png'),
('독서', '책을 통한 지식 습득', 'https://example.com/icons/reading.png'),
('운동', '신체 건강과 피트니스', 'https://example.com/icons/fitness.png'),
('음악', '음악 감상 및 연주', 'https://example.com/icons/music.png');

-- Seed goal personas
INSERT INTO goal_personas (name, description, recommended_traits) VALUES
('리더', '팀을 이끄는 리더십 발휘', '{"traits": ["결단력", "책임감", "소통능력"]}'),
('창의적 사색가', '새로운 아이디어를 창출하는 사람', '{"traits": ["창의성", "호기심", "개방성"]}'),
('실행가', '목표를 향해 꾸준히 나아가는 사람', '{"traits": ["끈기", "계획성", "실행력"]}'),
('관계 중심', '타인과의 관계를 소중히 여기는 사람', '{"traits": ["공감능력", "배려", "친화력"]}');

-- Seed questions
INSERT INTO questions (interest_id, persona_id, content, difficulty_level, created_by) VALUES
(1, 1, '오늘 하루 가장 성취감을 느낀 순간은 언제였나요?', 'easy', 'system'),
(1, 1, '최근 극복한 어려움은 무엇이고, 어떻게 해결했나요?', 'medium', 'system'),
(2, 2, '가장 기억에 남는 여행지는 어디였고, 그 이유는 무엇인가요?', 'medium', 'system'),
(3, 2, '최근 읽은 책에서 가장 인상 깊었던 구절이나 아이디어는 무엇인가요?', 'medium', 'system'),
(4, 3, '운동을 통해 얻고 싶은 변화는 무엇인가요?', 'easy', 'system'),
(5, 4, '음악이 당신의 감정에 어떤 영향을 주나요?', 'deep', 'system'),
(NULL, 1, '당신이 중요하게 여기는 가치는 무엇이고, 그 이유는 무엇인가요?', 'deep', 'system'),
(NULL, 3, '올해 이루고 싶은 가장 큰 목표는 무엇인가요?', 'easy', 'system'),
(NULL, 4, '가장 소중한 사람과의 최근 대화에서 느낀 감정은 무엇인가요?', 'medium', 'system'),
(NULL, 2, '당신의 하루를 색깔로 표현한다면 무슨 색이고, 그 이유는 무엇인가요?', 'deep', 'system');

-- Seed sample user
INSERT INTO users (name, age, gender, email, password_hash) VALUES
('테스트유저', 28, 'other', 'test@example.com', '$2a$10$dummyHashForTestingPurposes');

-- Seed user progress for sample user
INSERT INTO user_progress (user_id, total_answers, consecutive_days, last_answered_at, self_awareness_level) VALUES
(1, 0, 0, NULL, 1);

-- Link sample user to interests
INSERT INTO user_interests (user_id, interest_id) VALUES
(1, 1),
(1, 3);

-- Link sample user to personas
INSERT INTO user_goal_personas (user_id, persona_id) VALUES
(1, 1),
(1, 3);
