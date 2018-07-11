function [subMatrices] = createAllSubMatrices(mat, subMatDim)

    dim = size(mat);
    subMatrices = zeros(dim);
    
    for i = 1:dim(1)- subMatDim(1)
       for j = 1:dim(2) - subMatDim(2)
          
           x_indices = i:i+subMatDim(1);
           y_indices = j:j+subMatDim(2);
           curr = mat(x_indices, y_indices);
           flippedCurr = rot90(curr, 2);
           
           
       end
    end

end

