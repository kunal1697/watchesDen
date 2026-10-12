-- add images and change prices to INR
ALTER TABLE products ADD COLUMN image_url VARCHAR(500);
ALTER TABLE products ADD COLUMN hover_image_url VARCHAR(500);

UPDATE products SET price = 124900, image_url = 'https://images.unsplash.com/photo-1523170335258-f5ed11844a49', hover_image_url = 'https://images.unsplash.com/photo-1727716919539-3cd4aaf7af99' WHERE name = 'Nebula Chrono X1';
UPDATE products SET price = 84900,  image_url = 'https://images.unsplash.com/photo-1524592094714-0f0654e20314', hover_image_url = 'https://images.unsplash.com/photo-1522312346375-d1a52e2b99b3' WHERE name = 'Nebula Drift';
UPDATE products SET price = 239000, image_url = 'https://images.unsplash.com/photo-1524805444758-089113d48a6d', hover_image_url = 'https://images.unsplash.com/photo-1548171838-b3d4c18a3a67' WHERE name = 'Quantum Core';
UPDATE products SET price = 164900, image_url = 'https://images.unsplash.com/photo-1639736922209-793b59a41572', hover_image_url = 'https://images.unsplash.com/photo-1609587312208-cea54be969e7' WHERE name = 'Quantum Flux';
UPDATE products SET price = 179900, image_url = 'https://images.unsplash.com/photo-1600003014755-ba31aa59c4b6', hover_image_url = 'https://images.unsplash.com/photo-1620625515032-6ed0c1790c75' WHERE name = 'Solaris Gold';
UPDATE products SET price = 149900, image_url = 'https://images.unsplash.com/photo-1622434641406-a158123450f9', hover_image_url = 'https://images.unsplash.com/photo-1547996160-81dfa63595aa' WHERE name = 'Solaris Eclipse';
UPDATE products SET price = 112900, image_url = 'https://images.unsplash.com/photo-1691865179028-1729b766a5cd', hover_image_url = 'https://images.unsplash.com/photo-1762708052123-7332966eaecb' WHERE name = 'Abyss Diver 300';
UPDATE products SET price = 132900, image_url = 'https://images.unsplash.com/photo-1600003014608-c2ccc1570a65', hover_image_url = 'https://images.unsplash.com/photo-1639006570490-79c0c53f1080' WHERE name = 'Abyss Trench';
UPDATE products SET price = 59900,  image_url = 'https://images.unsplash.com/photo-1542496658-e33a6d0d50f6', hover_image_url = 'https://images.unsplash.com/photo-1539874754764-5a96559165b0' WHERE name = 'Pulse Smart Hybrid';
UPDATE products SET price = 289000, image_url = 'https://images.unsplash.com/photo-1623998021661-dc7555b2213d', hover_image_url = 'https://images.unsplash.com/photo-1639006570490-79c0c53f1080' WHERE name = 'Nebula Aurora';

UPDATE customization_options SET price_delta = 0     WHERE id IN ('case-steel', 'dial-void', 'strap-rubber', 'hands-ice');
UPDATE customization_options SET price_delta = 12000 WHERE id = 'case-titanium';
UPDATE customization_options SET price_delta = 22000 WHERE id = 'case-ceramic';
UPDATE customization_options SET price_delta = 28000 WHERE id = 'case-rosegold';
UPDATE customization_options SET price_delta = 5000  WHERE id IN ('dial-nebula', 'dial-pearl');
UPDATE customization_options SET price_delta = 7000  WHERE id = 'dial-aurora';
UPDATE customization_options SET price_delta = 4000  WHERE id = 'strap-leather';
UPDATE customization_options SET price_delta = 8000  WHERE id = 'strap-mesh';
UPDATE customization_options SET price_delta = 3000  WHERE id = 'strap-neon';
UPDATE customization_options SET price_delta = 1500  WHERE id IN ('hands-plasma', 'hands-volt');
UPDATE customization_options SET price_delta = 3500  WHERE id = 'hands-solar';
