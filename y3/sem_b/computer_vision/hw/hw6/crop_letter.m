function [cropped] = crop_letter(letter)

horizon = sum(letter);
h_indices = find(horizon~=0);
h_start = h_indices(1);
h_end = h_indices(end);

vertical = sum(letter,2);
v_indices = find(vertical~=0);
v_start = v_indices(1);
v_end = v_indices(end);


cropped = letter(v_start:v_end, h_start:h_end);


end

