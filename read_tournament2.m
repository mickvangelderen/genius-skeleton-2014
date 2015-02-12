function [ data ] = read_tournament2( filename )
%read_tournament2 Read tournament csv for two agent runs
%   Detailed explanation
    fid = fopen(filename);
    fgetl(fid); % skip sep=;
    fgetl(fid); % skip headers
    data = textscan(fid, '%d%s%d%s%s%d%s%s%s%s%s%s%s%s%s', 'Delimiter',';');
    % convert strings with doubles where necessary
    for i = [2 7:11 14 15];
        data{i} = str2double(strrep(data{:, i}, ',', '.'));
    end
    for i = [12 13]
        for j = 1:length(data{i})
            tokens = regexp(data{i}{j}, '\.([\w\d])+@', 'tokens');
            data{i}{j} = tokens{1};
        end
        data{i} = [data{i}{:}]';
    end
    fclose(fid);
end
