export function groupBy(array, key) {
  return array.reduce((result, currentValue) => {
    const groupKey = currentValue[key];
    if (!result[groupKey]) {
      result[groupKey] = [];
    }
    // Add the current value to the group
    result[groupKey].push(currentValue);
    return result;
  }, {});
}

/**
 * Return a bunch of categorized muscleGroups
 * @param {Array} muscleGrps 
 * @returns 
 */
export function getMuscleGrpOptions(muscleGrps) {

  return (
    Object.entries(groupBy(muscleGrps, 'group')).map(([group, items], index) => (
      <optgroup key={index} label={group}>
        {items.map((grp, i) => (
          <option key={i} value={grp.name} title={grp.medicalName.replace('_', ' ')}>
            {grp.name.replace("_", " ")}
          </option>
        ))}
      </optgroup>
    ))
  );
}