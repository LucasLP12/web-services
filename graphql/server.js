const express = require('express');
const { graphqlHTTP } = require('express-graphql');
const { buildSchema } = require('graphql');

const users = {
    1: { id: '1', nom: 'Alice', email: 'alice@example.com' },
    2: { id: '2', nom: 'Bob',   email: 'bob@example.com'   }
};

const schema = buildSchema(`
  type User {
    id: ID!
    nom: String!
    email: String!
  }

  type Query {
    hello: String
    getUser(id: ID!): User
  }
`);

const root = {
    hello: () => 'Hello, GraphQL!',
    getUser: ({ id }) => users[id] || null
};

const app = express();

app.use('/graphql', graphqlHTTP({
    schema: schema,
    rootValue: root,
    graphiql: true   
}));

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Serveur demarre sur http://localhost:${PORT}/graphql`);
});
