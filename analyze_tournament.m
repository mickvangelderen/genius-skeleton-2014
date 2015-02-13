data = read_tournament('Log-Tournament_20150213-154242.csv');

nagentsPerSession = (length(data) - 11)/2;
agents = unique([data{11 + (1:nagentsPerSession)}]);
nagents = length(agents);

utilities = cell(1, nagents);

% compute gained utility for each agent
for iagents = 1:nagents
    agent = agents{iagents};
    accumulator = [];
    for iaps = 1:nagentsPerSession
        indices = strcmp(data{11 + iaps}, agent);
        utils = data{11 + nagentsPerSession + iaps}(indices);
        accumulator = [accumulator;utils];
    end
    utilities{iagents} = accumulator;
end

agents'
means = mean(cell2mat(utilities));
stddevs = std(cell2mat(utilities));
figure
hold on
bar(1:nagents, means)
errorbar(1:nagents, means, stddevs, '.')