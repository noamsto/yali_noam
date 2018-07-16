function [J, I] = findLetter(mat, letter, thresh)

    if nargin < 3
        thresh = 0.99;
    end
    cm = correlationMatrix(mat, letter);
    [J, I] = find(cm > thresh);
    
end

