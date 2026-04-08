INSERT INTO `orders`(`id`, `created_at`, `customer_name`, `status`) VALUES
(1,  '2025-03-01 10:15:00', 'John Smith', 'completed'),
(2,  '2025-03-01 11:30:00', 'Emma Johnson', 'pending'),
(3,  '2025-03-02 09:45:00', 'Michael Brown', 'processing'),
(4,  '2025-03-02 14:20:00', 'Sophia Lee', 'completed'),
(5,  '2025-03-03 08:00:00', 'James Wilson', 'cancelled'),
(6,  '2025-03-03 12:10:00', 'Olivia Martinez', 'pending'),
(7,  '2025-03-04 15:30:00', 'William Davis', 'completed'),
(8,  '2025-03-04 17:45:00', 'Ava Garcia', 'processing'),
(9,  '2025-03-05 09:00:00', 'Benjamin Rodriguez', 'pending'),
(10, '2025-03-05 13:25:00', 'Mia Hernandez', 'completed');

INSERT INTO `product_quantity`(`id`, `product_id`, `quantity`) VALUES
(1, 1, 5),
(2, 2, 3),
(3, 3, 8),
(4, 4, 2),
(5, 5, 10),
(6, 6, 1),
(7, 7, 6),
(8, 8, 4),
(9, 9, 7),
(10, 10, 9);