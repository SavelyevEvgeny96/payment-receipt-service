-- 1. Добавляем колонку
ALTER TABLE checkout_mappings
ADD COLUMN group_code varchar(255);

-- 2. Заполняем для ALL
UPDATE checkout_mappings
SET group_code = 'ALL_GROUP_CODE'
WHERE product = 'ALL';

-- 3. Заполняем для OSAGO
UPDATE checkout_mappings
SET group_code = 'OSAGO_GROUP_CODE'
WHERE product = 'OSAGO';