import json

def calculate_price(valoracion, edad):
    price_multiplier = 35000.0
    precio_base = (valoracion * valoracion) * price_multiplier
    factor_edad = 1.0
    if edad > 30:
        factor_edad = max(0.1, 1.0 - (edad - 30) * 0.05)
    
    precio_final = round(precio_base * factor_edad)
    # Convert to millions for the JSON format (currently it was 55.00, etc.)
    # Wait, the JSON had 55.00. 97^2 * 35000 = 329,315,000.
    # So if 55.00 was 55M, then 329,315,000 should be 329.32M.
    return round(precio_final / 1_000_000.0, 2)

with open('/home/hector/Escritorio/F1_Manager/backend/src/main/resources/initial_data.json', 'r') as f:
    data = json.load(f)

# Create a map for estadisticas by id
stats_map = {s['id']: s for s in data['estadisticas']}

for piloto in data['pilotos']:
    stats_id = piloto['estadistica_id']
    stats = stats_map[stats_id]
    valoracion = stats['valoracion']
    edad = piloto['edad']
    new_price = calculate_price(valoracion, edad)
    print(f"Driver: {piloto['nombre']} | Age: {edad} | Rating: {valoracion} | Old Price: {piloto['valor']} | New Price: {new_price}")
    piloto['valor'] = new_price

with open('/home/hector/Escritorio/F1_Manager/backend/src/main/resources/initial_data.json', 'w') as f:
    json.dump(data, f, indent=2)
