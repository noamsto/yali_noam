function [cropped] = crop_letter(letter)

horizon = sum(letter);
h_indices = find(horizon~=0);
h_start = h_indices(1);
if h_start~= 1; h_start = h_start-1; end
h_end = h_indices(end);
if h_end ~= size(letter,2); h_end = h_end+1; end

vertical = sum(letter,2);
v_indices = find(vertical~=0);
v_start = v_indices(1);
if v_start~= 1; v_start = v_start-1; end
v_end = v_indices(end);
if v_end~= size(letter,1); v_end = v_end+1; end



cropped = letter(v_start:v_end, h_start:h_end);


end

