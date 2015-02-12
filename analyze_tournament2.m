data = read_tournament2('Log-Tournament_20150212-162959.csv');

agents = unique([data{[12 13]}]);
nagents = length(agents);

utilities = cell(1, nagents);

% compute gained utility for each agent
for iagents = 1:nagents
    agent = agents{iagents};
    first = data{14}(strcmp(data{12}, agent));
    last = data{15}(strcmp(data{13}, agent));
    utilities{iagents} = [first;last];
end

agents'
means = mean(cell2mat(utilities));
stddevs = std(cell2mat(utilities));
figure
hold on
bar(1:3, means)
errorbar(1:3, means, stddevs, '.')