Welcome to the Most Popular NY Times Sections API!

## Endpoints
[GET] - /api/mostpopular

---

### [GET] - /api/mostpopular
Returns an ordered list of the most popular NY times sections with a count of their occurrences in the
`most emailed`, `most viewed` and `most shared` categories respectively.

#### Queries
|Parameter|Default|Description|
|---------|-------|-----------|
|limit | 10 | The maximum number of top sections to return. For example, if you want the top 5, set limit as such.|
|timePeriod|7| Number of days corresponding to a period of content. Allowed value is 1, 7 or 30.|

#### Example
[GET] - http://{dns}/api/mostpopular?limit=2
#### Responses
_good_
```json
{
  "success" : true,
  "status" : 200,
  "results" : 2,
  "data" : [
    {
      "section" : "Sports",
      "appearedIn" : {
        "mostShared" : 37,
        "mostViewed" : 100,
        "mostEmailed" : 13
      }
    },
    {
      "sections" : "Education",
      "appearedIn" : {
        "mostShared" : 32,
        "mostViewed" : 95,
        "mostEmailed" : 12
      }
    }
  ]
}
```
_errored_
```json
{
  "success" : false,
  "message" : "Invalid parameter set"
}
```