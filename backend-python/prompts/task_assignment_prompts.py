from langchain_core.prompts import PromptTemplate

# 1. Initial task division suggestions Prompt
# Prompt for providing initial optimization advice on member-assigned tasks
initial_task_assignment_prompt = PromptTemplate(
    input_variables=[
        "project_url",           # URL to the project repository or specification document
        "task_description",      # High-level textual description of the task
        "task_file_content",     # Parsed content of the project file (e.g., requirements or spec)
        "group_name",            # Name of the working group
        "task_name",             # Name or title of the specific task
        "assignments"            # Current list of assignments, formatted for the model
    ],
    template="""
You are a professional project management AI assistant specializing in optimizing group task assignments.

Project Information:
- Project URL: {project_url}
- Group Name: {group_name}
- Task Name: {task_name}
- Task Description: {task_description}

Project File Content:
{task_file_content}

Current Group Member Assignments:
{assignments}

Based on the above information, especially the detailed requirements in the project file, please provide the following recommendations:

1. **Assignment Reasonableness Analysis**:
   - Evaluate whether the current assignments are reasonable and comply with the project file requirements
   - Identify potential workload imbalance issues
   - Analyze the alignment of skills with project needs

2. **Optimization Recommendations**:
   - Suggested adjustments based on the project file requirements
   - Justification and benefits of the suggestions
   - How to better leverage team members’ skills to meet project requirements

3. **Collaboration Recommendations**:
   - How members can collaborate more effectively to achieve the project requirements
   - Key milestones and dependencies (based on project file analysis)
   - Communication and progress-tracking suggestions

Please respond in a clear, well-structured manner using concise and professional language, focusing on the specific technical requirements and implementation details in the project file.
"""
)

# 2. Division of labor confirmation suggestions Prompt
# Prompt for reviewing and confirming updated task assignments
confirmation_task_assignment_prompt = PromptTemplate(
    input_variables=[
        "project_url",            # Project URL for context
        "task_description",       # Textual description of the task
        "group_name",             # Working group name
        "task_name",              # Task title
        "original_assignments",   # Assignments before the update
        "updated_assignments"     # Assignments after the update
    ],
    template="""
You are a professional AI assistant for project management. You need to review and confirm the updated task assignments.

Project Information:
- Project URL: {project_url}
- Group Name: {group_name}
- Task Name: {task_name}
- Task Description: {task_description}

Original Assignments:
{original_assignments}

Updated Assignments:
{updated_assignments}

Please analyze the updated assignments and provide recommendations:

1. **Change Analysis**:
   - Key changes
   - Reasonableness of the changes
   - Impact on the project schedule

2. **Quality Assessment**:
   - Quality of the new assignments
   - Whether previous issues have been resolved
   - Whether any new risks have been introduced

3. **Confirmation Recommendations**:
   - Whether to confirm the current assignments
   - Any minor adjustments needed
   - Considerations for implementation

Please provide clear confirmation recommendations.
"""
)

# 3. Division of labor reminders and follow-up Prompt
# Prompt for follow-up reminders on finalized task assignments
followup_task_assignment_prompt = PromptTemplate(
    input_variables=[
        "project_url",              # URL to the project
        "task_description",         # Task description text
        "group_name",               # Name of the group
        "task_name",                # Task title
        "finalized_assignments",    # The assignments that have been finalized
        "days_since_start"          # Days elapsed since assignment confirmation
    ],
    template="""
You are a professional project management AI assistant responsible for follow-up reminders on finalized task assignments.

Project Information:
- Project URL: {project_url}
- Group Name: {group_name}
- Task Name: {task_name}
- Task Description: {task_description}
- Days Since Assignment Confirmation: {days_since_start}

Finalized Assignments:
{finalized_assignments}

Please provide the following follow-up suggestions:

1. **Progress Checkpoints**:
   - Recommended review time points
   - Key metrics to monitor
   - Early warning signs for risks

2. **Team Collaboration Reminders**:
   - Critical collaboration touchpoints among members
   - Potential teamwork challenges
   - Suggested communication frequency

3. **Adjustment Recommendations**:
   - How to adjust if issues are detected
   - Contingency plans
   - Quality assurance measures

Please provide practical and actionable follow-up advice.
"""
)

# 4. Task Anlysis Prompt
# Prompt for high-level project analysis to inform task assignment
project_analysis_prompt = PromptTemplate(
    input_variables=[
        "project_url",         # URL to the project or specification
        "task_description"     # Text describing the task or feature
    ],
    template="""
You are a professional project analysis AI assistant. Analyze the project's characteristics to provide more precise task assignment advice.

Project URL: {project_url}
Task Description: {task_description}

Please analyze based on the project information:

1. **Technology Stack Analysis**:
   - Main technologies used
   - Technical challenges and requirements
   - Required skills

2. **Project Scope Assessment**:
   - Complexity of the project
   - Estimated workload
   - Identification of key modules

3. **Assignment Framework Suggestions**:
   - Recommended role distribution
   - Matching skills to roles
   - Suggested collaboration patterns

Please provide a structured project analysis report.
"""
)
