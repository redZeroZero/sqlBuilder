CREATE TABLE departments (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    department_id INTEGER REFERENCES departments(id),
    hire_date DATE NOT NULL,
    salary NUMERIC(10,2) NOT NULL
);

CREATE TABLE jobs (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    salary NUMERIC(10,2) NOT NULL,
    employee_id INTEGER REFERENCES employees(id)
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    price NUMERIC(10,2) NOT NULL
);

CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id),
    order_date TIMESTAMP NOT NULL DEFAULT NOW(),
    total NUMERIC(12,2) NOT NULL
);

CREATE TABLE order_lines (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id),
    product_id INTEGER REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL
);

CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id),
    amount NUMERIC(12,2) NOT NULL,
    paid_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO departments (name) VALUES
('Engineering'),
('Finance'),
('Marketing');

INSERT INTO employees (first_name, last_name, department_id, hire_date, salary) VALUES
('Alice','Martin',1,'2015-03-01',95000),
('Bob','Dubois',1,'2017-08-15',88000),
('Clara','Leroy',2,'2018-01-10',78000),
('David','Marchand',3,'2019-07-23',67000);

INSERT INTO jobs (title, salary, employee_id) VALUES
('Senior Engineer',120000,1),
('Lead Engineer',110000,2),
('Financial Analyst',90000,3),
('Marketing Coordinator',65000,4);

INSERT INTO products (name, category, price) VALUES
('Widget Pro','Hardware',199.99),
('Widget Lite','Hardware',99.99),
('Service Plan','Services',299.00),
('Consulting','Services',500.00);

INSERT INTO customers (first_name, last_name, email) VALUES
('Émilie','Bernard','emilie.bernard@example.com'),
('Thierry','Rousseau','thierry.rousseau@example.com'),
('Luc','Germain','luc.germain@example.com');

INSERT INTO orders (customer_id, order_date, total) VALUES
(1, '2023-11-02', 499.98),
(2, '2023-11-12', 299.00),
(3, '2023-11-20', 699.99);

INSERT INTO order_lines (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 2, 199.99),
(2, 3, 1, 299.00),
(3, 4, 1, 500.00),
(3, 2, 2, 99.99);

INSERT INTO payments (order_id, amount) VALUES
(1, 499.98),
(2, 299.00),
(3, 699.99);
