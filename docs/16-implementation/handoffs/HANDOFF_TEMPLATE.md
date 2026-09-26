# Work packet handoff

WORK PACKET:

RESULT:
COMPLETE / PARTIAL / BLOCKED

INTEGRATION BRANCH:

BASE SHA:

WORK BRANCH:

IMPLEMENTATION HEAD:

FINAL BRANCH HEAD:

## COMMITS

## FILES CHANGED

## WHAT CHANGED

## WHAT DID NOT CHANGE

## TESTS EXECUTED

- command
- result

## PHYSICAL / BEHAVIORAL EVIDENCE

## IDEA / DESIGN IMPACT

## DEVIATIONS FROM PACKET

## KNOWN RISKS

## UNRESOLVED

## FOLLOW-UP CANDIDATES

## WORKTREE STATUS

## PUSH STATUS

## PR

## HEAD SEMANTICS

- `IMPLEMENTATION HEAD` is the commit containing the substantive completed packet work before purely administrative handoff/push-status commits, if such commits exist.
- `FINAL BRANCH HEAD` is the exact pushed remote branch SHA presented for Anchor review/PR/CI.
- If there is no administrative tail, both fields may be identical.
- Anchor acceptance and CI should always identify the exact `FINAL BRANCH HEAD` under review.