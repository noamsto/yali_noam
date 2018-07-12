function img=read_data(fname)
% close ALL
%fid=fopen('unknown_text_s.dat','r') ;
fid=fopen(fname,'r') ;
c=fread(fid,2,'uint16') ;
m=c(1);
n=c(2);
for i=1:m
    for j=1:n
        img(i,j)=fread(fid,1,'uint16') ;
    end
end
fclose(fid);
% figure(1)
% image(img)
% colormap(gray)